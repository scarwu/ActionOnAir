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

    private final Activity activity;
    private final Context context;

    private CallbackHandler callbackHandler;

    public interface CallbackHandler {
        public void onTAGReceive(Tag tag);
    }

    private NfcAdapter nfcAdapter;

    private PendingIntent nfcPendingIntent;

    private IntentFilter[] nfcFilters;

    /**
     * Constructor
     *
     * @param appContext
     * @param appCallbackHandler
     */

    public NFCDevice(Activity appActivity, Context appContext, CallbackHandler appCallbackHandler) {
        activity = appActivity;
        context = appContext;
        callbackHandler = appCallbackHandler;

        // Initialize
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        nfcPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // NFC Action Filter Actions
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("addDataTypeFail", e);
        }

        nfcFilters = new IntentFilter[] {
            filter
        };
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

    public void onPause() {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    public void onResume() {
        nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, nfcFilters, null);
    }
}