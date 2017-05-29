/**
 * Sony - Action Cam
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs.camera;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.IOException;

public class SonyActionCam {

    public static final String MIME_TYPE = "application/x-sony-pmm";

    public SonyActionCam() {

    }

    public void discover() {

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

            Log.i("NFC", "NdefMessage: " + message);

            NdefRecord[] records = message.getRecords();

            for (NdefRecord record: records) {
                String recordType = new String(record.getType());

                Log.i("NFC", "NdefRecordType: " + recordType);
                Log.i("NFC", "NdefRecordPayload: " + new String(record.getPayload()));

                if (MIME_TYPE.equals(new String(record.getType()))) {
                    setting = decodePayload(record.getPayload());

                    if (null != setting) {
                        Log.i("NFC", "WifiSSID: " + setting[0]);
                        Log.i("NFC", "WifiPass: " + setting[1]);
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e("NFC", "NullPointerException", e);
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