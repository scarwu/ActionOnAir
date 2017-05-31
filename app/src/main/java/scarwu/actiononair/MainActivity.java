/**
 * Main Activity
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.nfc.Tag;

import java.util.ArrayList;

// Custom Libs
import scarwu.actiononair.libs.DBHelper;
import scarwu.actiononair.libs.WifiDevice;
import scarwu.actiononair.libs.NFCDevice;
import scarwu.actiononair.libs.FontManager;
import scarwu.actiononair.sns.Facebook;
import scarwu.actiononair.sns.Google;
import scarwu.actiononair.cameras.SonyActionCam;

// Facebook Libs
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.CallbackManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

// Google Libs
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AoA-" + MainActivity.class.getSimpleName();

    private Activity appActivity;
    private Context appContext;

    // SQLite
    private DBHelper dbHelper;

    // Devices
    private WifiDevice wifiDevice;
    private NFCDevice nfcDevice;

    // Facebook
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;

    // Google
    private GoogleApiClient googleApiClient;
    private static final String GOOGLE_CLIENT_ID = "156988006491-umvot01al8ic6p99nqd7qn4rqguspcg5.apps.googleusercontent.com";
    private static final int GOOGLE_SIGN_IN = 9001;

    // Flags
    private boolean isFacebookAuth = false;
    private boolean isGoogleAuth = false;
    private String cameraProvider = null;

    private String[] cameraProviderArray;
    private String[] cameraSSIDArray;

    // Widgets
    private Button snsFacebookStatus;
    private Button snsGoogleStatus;
    private ListView cameraList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Create");

        setContentView(R.layout.activity_main);

        // Get Application Objects
        appActivity = this;
        appContext = getApplicationContext();

        // DB Helper
        dbHelper = new DBHelper(appContext);

        // Init Devices
        wifiDevice = new WifiDevice(appContext, new WifiDevice.CallbackHandler() {

            @Override
            public void onSSIDChange(String ssid) {

                // Refresh Camera List
                refreshCameraList();
            }
        });

        nfcDevice = new NFCDevice(appContext, new NFCDevice.CallbackHandler() {

            @Override
            public void onTAGReceive(Tag tag) {

                String[] setting = SonyActionCam.resolveTagAndGetWifiSetting(tag);

                if (null == setting) {
                    return;
                }

                // Add Camera
                dbHelper.addCameraItem(setting[0], setting[1], "sony");

                // Refresh Camera List
                refreshCameraList();
            }
        });

        // Init SNS
        initSNSFacebook();
        initSNSGoogle();

        // Init Widgets
        initLocalWidgets();
        initGlobalWidgets();

        // Refresh Widgets
        refreshSNSFacebookStatus();
        refreshSNSGoogleStatus();
        refreshCameraList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "Pause");

        // NFC
        nfcDevice.onPause(appActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "Resume");

        // NFC
        nfcDevice.onResume(appActivity);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        Log.i(TAG, "NewIntent");

        // NFC
        nfcDevice.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "ActivityResult");
        Log.i(TAG, "ActivityResult: RequestCode: " + requestCode);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Init SNS Facebook
     */
    private void initSNSFacebook() {
        FacebookSdk.sdkInitialize(appContext);
        AppEventsLogger.activateApp(appContext);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();

                String applicationId = accessToken.getApplicationId();
                String userId = accessToken.getUserId();
                String token = accessToken.getToken();

                Log.i(TAG, "Facebook: LoginSuccess");
                Log.i(TAG, "Facebook: ApplicationId: " + applicationId);
                Log.i(TAG, "Facebook: UserId: " + userId);
                Log.i(TAG, "Facebook: Token: " + token);

                dbHelper.removeSNSItem("facebook");
                dbHelper.addSNSItem("facebook", token);

                // Refresh SNS Facebook Status
                refreshSNSFacebookStatus();
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Facebook: LoginCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i(TAG, "Facebook: LoginError");
            }
        });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.i(TAG, "Facebook: Logout");

                    dbHelper.removeSNSItem("facebook");

                    // Refresh SNS Facebook Status
                    refreshSNSFacebookStatus();
                }
            }
        };

        accessTokenTracker.startTracking();
    }

    /**
     * Init SNS Google
     */
    private void initSNSGoogle() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(new Scope(Scopes.EMAIL))
//            .requestServerAuthCode(GOOGLE_CLIENT_ID)
            .requestEmail()
            .build();

        googleApiClient = new GoogleApiClient.Builder(appContext)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();
    }

    /**
     * Handle Google Sign In Result
     *
     * @param result
     */
    private void handleGoogleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {

            GoogleSignInAccount account = result.getSignInAccount();

            String id = account.getId();
            String serverAuthCode = account.getServerAuthCode();

            Log.i(TAG, "Google: LoginSuccess");
            Log.i(TAG, "Google: Id: " + id);
            Log.i(TAG, "Google: ServerAuthCode: " + serverAuthCode);

            dbHelper.removeSNSItem("google");
            dbHelper.addSNSItem("google", id);

            // Refresh SNS Google Status
            refreshSNSGoogleStatus();
        } else {
            int code = result.getStatus().getStatusCode();

            Log.i(TAG, "Google: LoginFail");
            Log.i(TAG, "Google: Code: " + code);
        }
    }

    /**
     * Init Local Widgets
     */
    private void initLocalWidgets() {

        // Facebook Widgets
        Button snsFacebookAuth = (Button) findViewById(R.id.snsFacebookAuth);
        Button snsFacebookLive = (Button) findViewById(R.id.snsFacebookLive);

        // SNS Facebook Auth
        snsFacebookAuth.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
        snsFacebookAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                facebookLoginButton.callOnClick();
            }
        });

        // SNS Facebook Live
        snsFacebookLive.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
        snsFacebookLive.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isFacebookAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Facebook Account", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Switch Page
                goToControlPanelPage("facebook");
            }
        });

        // Google Widgets
        Button snsGoogleAuth = (Button) findViewById(R.id.snsGoogleAuth);
        Button snsGoogleLive = (Button) findViewById(R.id.snsGoogleLive);

        // SND Google Auth
        snsGoogleAuth.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
        snsGoogleAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.i(TAG, "Google: Click");

                if (isGoogleAuth) {
                    Log.i(TAG, "Google: Logout");

                    // TODO: Google Logout Error
//                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
//
//                        @Override
//                        public void onResult(Status status) {
//                            Log.i("AoA-Google", "Logout");
//
//                            dbHelper.removeSNSItem("google");
//
//                            // Refresh SNS Google Status
//                            refreshSNSGoogleStatus();
//                        }
//                    });

                    dbHelper.removeSNSItem("google");

                    // Refresh SNS Google Status
                    refreshSNSGoogleStatus();
                } else {
                    Log.i(TAG, "Google: Login");

                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
                }
            }
        });

        // SND Google Live
        snsGoogleLive.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
        snsGoogleLive.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isGoogleAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Google Account", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Switch Page
                goToControlPanelPage("google");
            }
        });
    }

    /**
     * Init Global Widgets
     */
    private void initGlobalWidgets() {

        // Status
        snsFacebookStatus = (Button) findViewById(R.id.snsFacebookStatus);
        snsFacebookStatus.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));

        // Status
        snsGoogleStatus = (Button) findViewById(R.id.snsGoogleStatus);
        snsGoogleStatus.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));

        // List
        cameraList = (ListView) findViewById(R.id.cameraList);
    }

    /**
     * Refresh SNS Facebook Status
     */
    private void refreshSNSFacebookStatus() {
        Cursor dbCursor = dbHelper.getSNSItem("facebook");

        if (0 != dbCursor.getCount()) {
            dbCursor.moveToFirst();

            String token = dbCursor.getString(dbCursor.getColumnIndex("token"));

            isFacebookAuth = !token.equals("");
        } else {
            isFacebookAuth = false;
        }

        if (isFacebookAuth) {
            snsFacebookStatus.setText(R.string.icon_link);
        } else {
            snsFacebookStatus.setText(R.string.icon_unlink);
        }
    }

    /**
     * Refresh SNS Google Status
     */
    private void refreshSNSGoogleStatus() {
        Cursor dbCursor = dbHelper.getSNSItem("google");

        if (0 != dbCursor.getCount()) {
            dbCursor.moveToFirst();

            String token = dbCursor.getString(dbCursor.getColumnIndex("token"));

            isGoogleAuth = !token.equals("");
        } else {
            isGoogleAuth = false;
        }

        if (isGoogleAuth) {
            snsGoogleStatus.setText(R.string.icon_link);
        } else {
            snsGoogleStatus.setText(R.string.icon_unlink);
        }
    }

    /**
     * Refresh Camera List
     */
    private void refreshCameraList() {

        ArrayList<String> cameraProviderList = new ArrayList<String>();
        ArrayList<String> cameraSSIDList = new ArrayList<String>();

        Cursor dbCursor = dbHelper.getCameraList();

        if (0 != dbCursor.getCount()) {
            dbCursor.moveToFirst();

            while (!dbCursor.isAfterLast()) {
                String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));
                String provider = dbCursor.getString(dbCursor.getColumnIndex("provider"));

                Log.i(TAG, "CameraList: Item: " + ssid + ", " + provider);

                cameraSSIDList.add(ssid);
                cameraProviderList.add(provider);

                dbCursor.moveToNext();
            }
        }

        cameraSSIDArray = (String[]) cameraSSIDList.toArray(new String[cameraSSIDList.size()]);
        cameraProviderArray = (String[]) cameraProviderList.toArray(new String[cameraProviderList.size()]);

        Log.i(TAG, "CameraList: Set Adapter");

        cameraList.setAdapter(new ListAdapter());
    }

    /**
     * List Adapter for Camera List
     */
    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return cameraSSIDArray.length;
        }

        @Override
        public Object getItem(int arg0) {
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int listItem, View listView, ViewGroup arg2) {
            listView = getLayoutInflater().inflate(R.layout.item_camera, null);

            // Widgets
            Button status = (Button) listView.findViewById(R.id.cameraStatus);
            Button ssid = (Button) listView.findViewById(R.id.cameraSSID);
            Button remove = (Button) listView.findViewById(R.id.removeCamera);

            // Status
            status.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));

            if (wifiDevice.getCurrentSSID().equals(cameraSSIDArray[listItem])) {
                status.setText(R.string.icon_connect);

                // Set Camera Provider
                cameraProvider = cameraProviderArray[listItem];
            }

            // SSID
            ssid.setText(cameraSSIDArray[listItem]);
            ssid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Cursor dbCursor = dbHelper.getCameraItem(cameraSSIDArray[listItem]);

                    if (0 == dbCursor.getCount()) {
                        return;
                    }

                    dbCursor.moveToFirst();

                    String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));
                    String pass = dbCursor.getString(dbCursor.getColumnIndex("pass"));
                    String provider = dbCursor.getString(dbCursor.getColumnIndex("provider"));

                    // Connect Camera Wifi Lan
                    if ("sony".equals(provider)) {
                        wifiDevice.connectAP(ssid, pass, "wpa");
                    }
                }
            });

            // Remove
            remove.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
            remove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    dbHelper.removeCameraItem(cameraSSIDArray[listItem]);

                    // Refresh Camera List
                    refreshCameraList();
                }
            });

            return listView;
        }
    }

    /**
     * Go to ControlPanel Page
     *
     * @param snsProvider
     */
    private void goToControlPanelPage(String snsProvider) {
        if (null == cameraProvider) {
            Toast.makeText(appContext, "Please Connect Your Camera Wifi", Toast.LENGTH_SHORT).show();

            return;
        }

        Log.i("AoA-Activity", "Switch");

        Intent intent = new Intent();
        intent.putExtra("snsProvider" , snsProvider);
        intent.putExtra("cameraProvider" , cameraProvider);
        intent.setClass(appContext , ControlPanelActivity.class);

        startActivity(intent);
    }
}
