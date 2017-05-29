/**
 * Main Activity
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.util.Log;

// Widgets
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

// Devices
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

// Custom Libs
import scarwu.actiononair.libs.DBHelper;
import scarwu.actiononair.libs.FontManager;
import scarwu.actiononair.libs.sns.Facebook;
import scarwu.actiononair.libs.sns.Google;
import scarwu.actiononair.libs.camera.SonyActionCam;

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

    // Widgets
    private ListView cameraList;

    // Flags
    private boolean isFacebookAuth = false;
    private boolean isGoogleAuth = false;
    private String cameraProvider = null;

    // Facebook
    private Button snsFacebookStatus;
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;

    // Google
    private Button snsGoogleStatus;
    private SignInButton googleLoginButton;
    private GoogleApiClient googleApiClient;
    private static final String GOOGLE_CLIENT_ID = "156988006491-umvot01al8ic6p99nqd7qn4rqguspcg5.apps.googleusercontent.com";
    private static final int GOOGLE_SIGN_IN = 9001;

    // Wifi
    WifiManager wifiManager;
    String currentSSID;

    // NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcFilters;

    // SQLite
    private DBHelper dbHelper;
	private Cursor dbCursor;

    private String[] cameraProviderArray;
    private String[] cameraSSIDArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // DB Helper
        dbHelper = new DBHelper(this);

        // Init Devices
        initWifiDevice();
        initNFCDevice();

        // Init SNS
        initSNSFacebook();
        initSNSGoogle();

        // Init Widgets
        initSNSFacebookWidgets();
        initSNSGoogleWidgets();
        initCameraWidgets();
    }

    /**
     * Init Wifi Device
     */
    private void initWifiDevice() {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Get Wifi Info
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        currentSSID = wifiInfo.getSSID().replace("\"", "");

        // Wifi Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        registerReceiver(new wifiReceiver(), intentFilter);
    }

    /**
     * Wifi Receiver
     */
    private class wifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Get Status
            String action = intent.getAction();

            Log.i("AoA-WifiReceiver", "Action: " + action);

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String newSSID = wifiInfo.getSSID().replace("\"", "");

                if (currentSSID.equals(newSSID)) {
                    return;
                }

                Log.i("AoA-WifiReceiver", "OldSSID: " + currentSSID);
                Log.i("AoA-WifiReceiver", "NewSSID: " + newSSID);

                // Assign new SSID
                currentSSID = newSSID;

                // Refresh Camera List
                refreshCameraList();
            }
        }
    }

    /**
     * Init NFC Device
     */
    private void initNFCDevice() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // NFC Action Filter Actions
        IntentFilter nfcIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            nfcIntentFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("addDataTypeFail", e);
        }

        nfcFilters = new IntentFilter[] {
            nfcIntentFilter
        };
    }

    /**
     * Init SNS Facebook
     */
    private void initSNSFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();

                String applicationId = accessToken.getApplicationId();
                String userId = accessToken.getUserId();
                String token = accessToken.getToken();

                Log.i("AoA-Facebook", "LoginSuccess");
                Log.i("AoA-Facebook", "ApplicationId: " + applicationId);
                Log.i("AoA-Facebook", "UserId: " + userId);
                Log.i("AoA-Facebook", "Token: " + token);

                dbHelper.removeSNSItem("facebook");
                dbHelper.addSNSItem("facebook", token);

                // Refresh SNS Facebook Status
                refreshSNSFacebookStatus();
            }

            @Override
            public void onCancel() {
                Log.i("AoA-Facebook", "LoginCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("AoA-Facebook", "LoginError");
            }
        });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.i("AoA-Facebook", "Logout");

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

        googleApiClient = new GoogleApiClient.Builder(this)
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

            Log.i("AoA-Google", "LoginSuccess");
            Log.i("AoA-Google", "Id: " + id);
            Log.i("AoA-Google", "ServerAuthCode: " + serverAuthCode);

            dbHelper.removeSNSItem("google");
            dbHelper.addSNSItem("google", id);

            // Refresh SNS Google Status
            refreshSNSGoogleStatus();
        } else {
            int code = result.getStatus().getStatusCode();

            Log.i("AoA-Google", "LoginFail");
            Log.i("AoA-Google", "Code: " + code);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("AoA-ActivityResult", "RequestCode: " + requestCode);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        // Get Status
        String action = intent.getAction();
        String type = intent.getType();

        Log.i("AoA-NFC", "IntentAction: " + action);
        Log.i("AoA-NFC", "IntentType: " + type);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
            && SonyActionCam.MIME_TYPE.equals(type)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String[] setting = SonyActionCam.resolveTagAndGetWifiSetting(tag);

            if (null != setting) {

                // Add Camera
                dbHelper.addCameraItem(setting[0], setting[1], "sony");

                // Refresh Camera List
                refreshCameraList();
            }
        }
    }

    /**
     * Refresh SNS Facebook Status
     */
    private void refreshSNSFacebookStatus() {
        dbCursor = dbHelper.getSNSItem("facebook");

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
     * Init SNS Facebook Widgets
     */
    private void initSNSFacebookWidgets() {

        // Widgets
        Button auth = (Button) findViewById(R.id.snsFacebookAuth);
        Button live = (Button) findViewById(R.id.snsFacebookLive);

        // Status
        snsFacebookStatus = (Button) findViewById(R.id.snsFacebookStatus);
        snsFacebookStatus.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));

        // Auth
        auth.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        auth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                facebookLoginButton.callOnClick();
            }
        });

        // Live
        live.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        live.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isFacebookAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Facebook Account", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Switch Page
                goToControlPanelPage("facebook");
            }
        });

        // Refresh SNS Google Status
        refreshSNSFacebookStatus();
    }

    /**
     * Refresh SNS Google Status
     */
    private void refreshSNSGoogleStatus() {
        dbCursor = dbHelper.getSNSItem("google");

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
     * Init SNS Google Widgets
     */
    private void initSNSGoogleWidgets() {

        // Widgets
        Button auth = (Button) findViewById(R.id.snsGoogleAuth);
        Button live = (Button) findViewById(R.id.snsGoogleLive);

        // Status
        snsGoogleStatus = (Button) findViewById(R.id.snsGoogleStatus);
        snsGoogleStatus.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));

        // Auth
        auth.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        auth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.i("AoA-Google", "Click");

                if (isGoogleAuth) {
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

                    Log.i("AoA-Google", "Logout");

                    dbHelper.removeSNSItem("google");

                    // Refresh SNS Google Status
                    refreshSNSGoogleStatus();
                } else {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
                }
            }
        });

        // Live
        live.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        live.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isGoogleAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Google Account", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Switch Page
                goToControlPanelPage("google");
            }
        });

        // Refresh SNS Google Status
        refreshSNSGoogleStatus();
    }

    /**
     * Init Camera Widgets
     */
    private void initCameraWidgets() {

        // List
        cameraList = (ListView) findViewById(R.id.cameraList);

        // Refresh Camera List
        refreshCameraList();
    }

    /**
     * Refresh Camera List
     */
    private void refreshCameraList() {

        ArrayList<String> cameraProviderList = new ArrayList<String>();
        ArrayList<String> cameraSSIDList = new ArrayList<String>();

        dbCursor = dbHelper.getCameraList();

        if (0 != dbCursor.getCount()) {
            dbCursor.moveToFirst();

            while (!dbCursor.isAfterLast()) {
                String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));
                String provider = dbCursor.getString(dbCursor.getColumnIndex("provider"));

                Log.i("AoA-CameraView", "Item: " + ssid + ", " + provider);

                cameraSSIDList.add(ssid);
                cameraProviderList.add(provider);

                dbCursor.moveToNext();
            }
        }

        cameraSSIDArray = (String[]) cameraSSIDList.toArray(new String[cameraSSIDList.size()]);
        cameraProviderArray = (String[]) cameraProviderList.toArray(new String[cameraProviderList.size()]);

        Log.i("AoA-CameraView", "Set Adapter");

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
            status.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));

            if (currentSSID.equals(cameraSSIDArray[listItem])) {
                status.setText(R.string.icon_connect);

                // Set Camera Provider
                cameraProvider = cameraProviderArray[listItem];
            }

            // SSID
            ssid.setText(cameraSSIDArray[listItem]);
            ssid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    dbCursor = dbHelper.getCameraItem(cameraSSIDArray[listItem]);

                    if (0 != dbCursor.getCount()) {

                        dbCursor.moveToFirst();

                        String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));
                        String pass = dbCursor.getString(dbCursor.getColumnIndex("pass"));
                        String provider = dbCursor.getString(dbCursor.getColumnIndex("provider"));

                        // Connect Camera Wifi Lan
                        connectWifi(ssid, pass, provider);
                    }
                }
            });

            // Remove
            remove.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
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
     * Connect Wifi
     *
     * @param ssid
     * @param pass
     * @param provider
     */
    private void connectWifi(String ssid, String pass, String provider) {

        // New Wifi Config
        WifiConfiguration newConf = new WifiConfiguration();

        newConf.SSID = "\"" + ssid + "\"";

        // Sony Action Cam use WPA
        if ("sony".equals(provider)) {
            newConf.preSharedKey = "\"" + pass + "\"";
            newConf.hiddenSSID = true;
            newConf.status = WifiConfiguration.Status.ENABLED;

            newConf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            newConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            newConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            newConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            newConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            newConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            newConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        } else {
            return;
        }

        // Open Wifi
        wifiManager.setWifiEnabled(true);

        // Get All Config
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        Integer networkId = null;

        for (WifiConfiguration currentConf : list) {
            if(currentConf.SSID == null
                || !currentConf.SSID.equals(newConf.SSID)) {

                Log.i("AoA-WifiConfig", "DisableSSID: " + currentConf.SSID.replace("\"", ""));

                wifiManager.disableNetwork(currentConf.networkId);

                continue;
            }

            networkId = currentConf.networkId;
            newConf.networkId = currentConf.networkId;

            // Update Network
            wifiManager.updateNetwork(newConf);
        }

        if (null == networkId) {

            // Add Network
            wifiManager.addNetwork(newConf);

            // Get All Config Again
            list = wifiManager.getConfiguredNetworks();

            for (WifiConfiguration currentConf : list) {
                if(currentConf.SSID == null
                    || !currentConf.SSID.equals(newConf.SSID)) {

                    continue;
                }

                networkId = currentConf.networkId;

                break;
            }
        }

        Log.i("AoA-WifiConfig", "EnableSSID: " + newConf.SSID.replace("\"", ""));

        // Disconnect Wifi Connection
        wifiManager.disconnect();

        wifiManager.enableNetwork(networkId, true);
        wifiManager.saveConfiguration();

        // Reconnect Wifi Connection
        wifiManager.reconnect();
    }

    /**
     * Go to ControlPanel Page
     *
     * @param snsProvider
     */
    private void goToControlPanelPage(String snsProvider) {
        if (null == cameraProvider) {
            return;
        }

        Log.i("AoA-Activity", "Switch");

        Intent intent = new Intent();
        intent.putExtra("snsProvider" , snsProvider);
        intent.putExtra("cameraProvider" , cameraProvider);
        intent.setClass(MainActivity.this , ControlPanelActivity.class);

        startActivity(intent);
    }
}
