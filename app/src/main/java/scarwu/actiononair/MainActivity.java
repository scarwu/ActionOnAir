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
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.view.View;
import android.util.Log;
import android.database.Cursor;

// Widgets
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import android.nfc.NfcAdapter;
import android.nfc.Tag;

// Custom Libs
import scarwu.actiononair.ControlPanelActivity;
import scarwu.actiononair.libs.platform.facebook.*;
import scarwu.actiononair.libs.platform.google.*;
import scarwu.actiononair.libs.camera.sony.ActionCam;
import scarwu.actiononair.libs.DBHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private Button snsFBAuth;
    private Button snsFBLive;
    private Button snsGoogleAuth;
    private Button snsGoogleLive;
    private Button cameraNFCReader;
    private ListView cameraList;

    private PopupWindow nfcPopupWindow;

    // Flags
    private boolean isFBAuth = false;
    private boolean isGoogleAuth = false;

    // NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcFilters;

    // SQLite
    private DBHelper dbHelper;
	private Cursor dbCursor;

    private ListAdapter cameraListAdapter;
    private Integer[] cameraIdArray;
    private String[] cameraSSIDArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // DB Helper
        dbHelper = new DBHelper(this);

        // Initailize View Widgets
        initSNSFacebookWidgets();
        initSNSGoogleWidgets();
        initCameraWidgets();

        // NFC
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

        // Auth
        snsFBAuth = (Button) findViewById(R.id.snsFBAuth);
        snsFBAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isFBAuth) {
                    // disconnect
                } else {
                    // connect
                }
            }
        });

        // Live
        snsFBLive = (Button) findViewById(R.id.snsFBLive);
        snsFBLive.setOnClickListener(new View.OnClickListener() {

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

        // Auth
        snsGoogleAuth = (Button) findViewById(R.id.snsGoogleAuth);
        snsGoogleAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isGoogleAuth) {
                    // disconnect
                } else {
                    // connect
                }
            }
        });

        // Live
        snsGoogleLive = (Button) findViewById(R.id.snsGoogleLive);
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
     * Init Camera Widgets
     */
    private void initCameraWidgets() {

        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_nfc, null);

        nfcPopupWindow = new PopupWindow(popupView);
        nfcPopupWindow.setTouchable(true);
        nfcPopupWindow.setOutsideTouchable(true);
//        nfcPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        // Reader
        cameraNFCReader = (Button) findViewById(R.id.cameraNFCReader);
        cameraNFCReader.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                nfcPopupWindow.showAsDropDown(view);
            }
        });

        // List
        cameraList = (ListView) findViewById(R.id.cameraList);

        // Refresh Camera List
        refreshCameraList();
    }

    /**
     * Refresh Camera List
     */
    private void refreshCameraList() {

        try {
            dbCursor = dbHelper.readCameras();
        } catch (NullPointerException e) {
            return;
        }

        ArrayList<Integer> cameraIdList = new ArrayList<Integer>();
        ArrayList<String> cameraSSIDList = new ArrayList<String>();

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
        cameraListAdapter = new ListAdapter();
        cameraList.setAdapter(cameraListAdapter);
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return cameraIdArray.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(final int listItem, View listView, ViewGroup arg2) {

            // TODO Auto-generated method stub
            listView = getLayoutInflater().inflate(R.layout.item_camera, null);

            Button ssid = (Button) listView.findViewById(R.id.cameraSSID);
            Button remove = (Button) listView.findViewById(R.id.removeCamera);

            ssid.setText(cameraSSIDArray[listItem]);
            ssid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    // TODO Auto-generated method stub
//                    callSkype(phoneArray[listItem]);
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    // TODO Auto-generated method stub
                    dbHelper.removeCamera(cameraIdArray[listItem]);

                    // Refresh Camera List
                    refreshCameraList();
                }
            });

            return listView;
        }
    }

    /**
     * Go to ControlPanel Page
     */
    private void goToControlPanelPage(String sns) {
        Intent intent = new Intent();
        intent.putExtra("sns" , sns);
        intent.setClass(MainActivity.this , ControlPanelActivity.class);

        startActivity(intent);
    }
}
