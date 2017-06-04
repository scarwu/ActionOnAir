/**
 * NFC Device
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import scarwu.actiononair.cameras.SonyActionCam;


public class NFCDevice {

    private static final String TAG = "AoA-" + NFCDevice.class.getSimpleName();

    private CallbackHandler callbackHandler;
    private NfcAdapter nfcAdapter;

    /**
     * Constructor
     *
     * @param context
     * @param appCallbackHandler
     */
    public NFCDevice(Context context, CallbackHandler appCallbackHandler) {
        callbackHandler = appCallbackHandler;

        // Initialize
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    /**
     * Callback Handler
     */
    public interface CallbackHandler {
        public void onTAGReceive(Tag tag);
    }

    public void onNewIntent(Intent intent) {

        // Get Status
        String action = intent.getAction();
        String type = intent.getType();

        Log.i(TAG, "Intent: Action: " + action);
        Log.i(TAG, "Intent: Type: " + type);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
            && SonyActionCam.MIME_TYPE.equals(type)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Callback
            callbackHandler.onTAGReceive(tag);
        }
    }

    public void onPause(Activity activity) {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    public void onResume(Activity activity) {

        nfcAdapter.disableForegroundDispatch(activity);

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Error handling MIME type.");
        }

        // set up foregound dispatch for reading purposes
        IntentFilter[] filters = new IntentFilter[] {
            filter
        };

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, null);
    }
}