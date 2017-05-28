/**
 * Main Activity
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair;

import android.nfc.FormatException;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Fragment;
import android.app.PendingIntent;
import android.view.View;
import android.util.Log;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.IsoDep;

// Custom Libs
import java.io.IOException;
import java.nio.charset.Charset;

import scarwu.actiononair.ControlPanelActivity;

import scarwu.actiononair.libs.platform.facebook.*;
import scarwu.actiononair.libs.platform.google.*;
import scarwu.actiononair.libs.camera.sony.ActionCam;
import scarwu.actiononair.libs.NFCReader;

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

    public static final String SONY_MIME_TYPE = "application/x-sony-pmm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initailize View Widgets
        initSNSFacebookWidgets();
        initSNSGoogleWidgets();
        initCameraWidgets();

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Action Filter Actions
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
        Log.i("NFC", "Intent: " + intent);

        String action = intent.getAction();
        String type = intent.getType();

        Log.i("NFC", "IntentAction: " + action);
        Log.i("NFC", "IntentType: " + type);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
            && SONY_MIME_TYPE.equals(type)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Ndef ndef = Ndef.get(tag);

            try {
                ndef.connect();

                NdefMessage message = ndef.getNdefMessage();

                Log.i("NFC", "NdefMessage: " + message);

                NdefRecord[] records = message.getRecords();

                for (NdefRecord record: records) {
                    String recordType = new String(record.getType());

                    Log.i("NFC", "NdefRecordType: " + recordType);
                    Log.i("NFC", "NdefRecordPayload: " + new String(record.getPayload()));

                    if (SONY_MIME_TYPE.equals(new String(record.getType()))) {
                        String[] ppm = decodeSonyPPMMessage(record.getPayload());

                        if (null != ppm) {
                            Log.i("NFC", "WifiSSID: " + ppm[0]);
                            Log.i("NFC", "WifiPass: " + ppm[1]);
                        }
                    }
                }
            } catch (FormatException e) {
                Log.e("NFC", "FormatException", e);
            } catch (IOException e) {
                Log.e("NFC", "IOException", e);
            } finally {
                if (ndef != null) {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                        Log.e("NFC", "Error closing tag...", e);
                    }
                }
            }
        }
    }

    private static String[] decodeSonyPPMMessage(byte[] payload) {

        try {
            int ssidBytesStart = 8;
            int ssidLength = payload[ssidBytesStart];

            byte[] ssidBytes = new byte[ssidLength];
            int ssidPointer = 0;
            for (int i=ssidBytesStart+1; i<=ssidBytesStart+ssidLength; i++) {
                ssidBytes[ssidPointer++] = payload[i];
            }
            String ssid = new String(ssidBytes);

            int passwordBytesStart = ssidBytesStart+ssidLength+4;
            int passwordLength = payload[passwordBytesStart];

            byte[] passwordBytes = new byte[passwordLength];

            int passwordPointer = 0;
            for (int i=passwordBytesStart+1; i<=passwordBytesStart+passwordLength; i++) {
                passwordBytes[passwordPointer++] = payload[i];
            }
            String password = new String(passwordBytes);

            return new String[] {ssid, password};

        } catch(Exception e) {
            return null;
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
