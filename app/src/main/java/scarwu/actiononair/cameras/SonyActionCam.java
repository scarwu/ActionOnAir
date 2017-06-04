/**
 * Sony Action Cam
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.cameras;

import java.io.IOException;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import scarwu.actiononair.cameras.sony.SSDPClient;
import scarwu.actiononair.cameras.sony.RemoteDevice;

public class SonyActionCam {

    private static final String TAG = "AoA-" + SonyActionCam.class.getSimpleName();

    public static final String MIME_TYPE = "application/x-sony-pmm";

    Context ctx;
    SSDPClient ssdpClient;

    private CallbackHandler callbackHandler;
    private String currentDeviceIP;

    public SonyActionCam(Context context, CallbackHandler appCallbackHandler) {
        ctx = context;
        callbackHandler = appCallbackHandler;
        ssdpClient = new SSDPClient();

        currentDeviceIP = "";
    }

    public interface CallbackHandler {
        public void onFoundDevice(String ipAdress);
    }

    public String getIPAddress() {
        return currentDeviceIP;
    }

    public void discover() {

        Log.i(TAG, "Discover");

        // Call SSDP Client
        ssdpClient.search(new SSDPClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final RemoteDevice device) {

                currentDeviceIP = device.getIpAddres();

                Log.i(TAG, "Callback: Current: " + currentDeviceIP);

                callbackHandler.onFoundDevice(currentDeviceIP);
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onErrorFinished() {

            }
        });
    }

    public void setting() {

    }

    /**
     * Resolve Tag and Get Wifi Setting
     *
     * @param tag
     * @return
     */
    public static String[] resolveTagAndGetWifiSetting(Tag tag) {

        String[] setting = null;
        Ndef ndef = Ndef.get(tag);

        try {
            ndef.connect();

            NdefMessage message = ndef.getNdefMessage();

            Log.i(TAG, "Resolve: NdefMessage: " + message);

            NdefRecord[] records = message.getRecords();

            for (NdefRecord record: records) {
                String recordType = new String(record.getType());

                Log.i(TAG, "Resolve: NdefRecordType: " + recordType);
                Log.i(TAG, "Resolve: NdefRecordPayload: " + new String(record.getPayload()));

                if (MIME_TYPE.equals(new String(record.getType()))) {
                    setting = decodePayload(record.getPayload());

                    if (null != setting) {
                        Log.i(TAG, "Resolve: WifiSSID: " + setting[0]);
                        Log.i(TAG, "Resolve: WifiPass: " + setting[1]);
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Resolve: NullPointerException", e);
        } catch (FormatException e) {
            Log.e(TAG, "Resolve: FormatException", e);
        } catch (IOException e) {
            Log.e(TAG, "Resolve: IOException", e);
        } finally {
            if (ndef != null) {
                try {
                    ndef.close();
                } catch (IOException e) {
                    Log.e(TAG, "Resolve: Error closing tag...", e);
                }
            }
        }

        return setting;
    }

    /**
     * Decode Sony Payload
     *
     * @param payload
     * @return
     */
    private static String[] decodePayload(byte[] payload) {

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

            return new String[] {
                ssid,
                password
            };
        } catch(Exception e) {
            return null;
        }
    }
}