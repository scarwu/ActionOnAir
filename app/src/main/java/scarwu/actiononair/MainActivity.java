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
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.View;
import android.util.Log;
import android.database.Cursor;

// Widgets
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import android.nfc.NfcAdapter;
import android.nfc.Tag;

// Custom Libs
import scarwu.actiononair.libs.DBHelper;
import scarwu.actiononair.libs.FontManager;
import scarwu.actiononair.libs.sns.Facebook;
import scarwu.actiononair.libs.sns.Google;
import scarwu.actiononair.libs.camera.sony.ActionCam;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private ListView cameraList;

    // Flags
    private boolean isFBAuth = false;
    private boolean isGoogleAuth = false;
    private String cameraProvider = null;

    // Wifi
    WifiManager wifiManager;

    // NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcFilters;

    // SQLite
    private DBHelper dbHelper;
	private Cursor dbCursor;

    private Integer[] cameraIdArray;
    private String[] cameraSSIDArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // DB Helper
        dbHelper = new DBHelper(this);

        // Wifi
        initWifiDevice();

        // NFC
        initNFCDevice();

        // Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Google

        // Initialize View Widgets
        initSNSFacebookWidgets();
        initSNSGoogleWidgets();
        initCameraWidgets();
    }

    /**
     * Init Wifi Device
     */
    private void initWifiDevice() {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        Log.i("AoA-NFC", "Intent: " + intent);

        // Get Status
        String action = intent.getAction();
        String type = intent.getType();

        Log.i("AoA-NFC", "IntentAction: " + action);
        Log.i("AoA-NFC", "IntentType: " + type);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
            && ActionCam.MIME_TYPE.equals(type)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String[] setting = ActionCam.resolveTagAndGetWifiSetting(tag);

            if (null != setting) {

                // Add Camera
                dbHelper.addCamera("sony", setting[0], setting[1]);

                // Refresh Camera List
                refreshCameraList();
            }
        }
    }

    /**
     * Init SNS Facebook Widgets
     */
    private void initSNSFacebookWidgets() {

        // Widgets
        Button status = (Button) findViewById(R.id.snsFBStatus);
        Button auth = (Button) findViewById(R.id.snsFBAuth);
        Button live = (Button) findViewById(R.id.snsFBLive);

        // Status
        status.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));

        // Auth
        auth.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        auth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (isFBAuth) {
                    new Facebook().account.disconnect();
                } else {
                    new Facebook().account.connect();
                }
            }
        });

        // Live
        live.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        live.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isFBAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Facebook Account", Toast.LENGTH_SHORT).show();

                    return;
                }

                // Switch Page
                goToControlPanelPage("fb");
            }
        });
    }

    /**
     * Init SNS Google Widgets
     */
    private void initSNSGoogleWidgets() {

        // Widgets
        Button status = (Button) findViewById(R.id.snsGoogleStatus);
        Button auth = (Button) findViewById(R.id.snsGoogleAuth);
        Button live = (Button) findViewById(R.id.snsGoogleLive);

        // Status
        status.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));

        // Auth
        auth.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
        auth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isGoogleAuth) {
                    new Google().account.disconnect();
                } else {
                    new Google().account.connect();
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

        ArrayList<Integer> cameraIdList = new ArrayList<Integer>();
        ArrayList<String> cameraSSIDList = new ArrayList<String>();

        dbCursor = dbHelper.readCameras();

        if (0 != dbCursor.getCount()) {
            dbCursor.moveToFirst();

            while (!dbCursor.isAfterLast()) {
                int id = dbCursor.getInt(dbCursor.getColumnIndex("id"));
                String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));

                Log.i("AoA-CameraView", "Item: " + id + ", " + ssid);

                cameraIdList.add(id);
                cameraSSIDList.add(ssid);

                dbCursor.moveToNext();
            }
        }

        cameraIdArray = (Integer[]) cameraIdList.toArray(new Integer[cameraIdList.size()]);
        cameraSSIDArray = (String[]) cameraSSIDList.toArray(new String[cameraSSIDList.size()]);

        Log.i("AoA-CameraView", "Set Adapter");

        cameraList.setAdapter(new ListAdapter());
    }

    /**
     * List Adapter for Camera List
     */
    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return cameraIdArray.length;
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

            // SSID
            ssid.setText(cameraSSIDArray[listItem]);
            ssid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    dbCursor = dbHelper.getCamera(cameraIdArray[listItem]);

                    if (0 != dbCursor.getCount()) {

                        dbCursor.moveToFirst();

                        String provider = dbCursor.getString(dbCursor.getColumnIndex("provider"));
                        String ssid = dbCursor.getString(dbCursor.getColumnIndex("ssid"));
                        String pass = dbCursor.getString(dbCursor.getColumnIndex("pass"));

                        // Connect Camera Wifi Lan
                        connectWifi(provider, ssid, pass);
                    }
                }
            });

            // Remove
            remove.setTypeface(FontManager.getTypeface(MainActivity.this, FontManager.FONTAWESOME));
            remove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    dbHelper.removeCamera(cameraIdArray[listItem]);

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
     */
    private void connectWifi(String provider, String ssid, String pass) {

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

        wifiManager.addNetwork(newConf);
        wifiManager.setWifiEnabled(true);

        // Get All Config
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration tmpConf : list) {
            if(tmpConf.SSID == null
                || tmpConf.SSID.equals(newConf.SSID)) {

                continue;
            }

            Log.i("AoA-WifiConfig", "SSID: " + newConf.SSID);

            wifiManager.disconnect();
            wifiManager.enableNetwork(tmpConf.networkId, true);
            wifiManager.reconnect();

            break;
        }
    }

    /**
     * Go to ControlPanel Page
     */
    private void goToControlPanelPage(String sns) {
        if (null == cameraProvider) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("sns" , sns);
        intent.putExtra("provider" , cameraProvider);
        intent.setClass(MainActivity.this , ControlPanelActivity.class);

        startActivity(intent);
    }
}
