/**
 * Wifi Device
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import scarwu.actiononair.MainActivity;

public class WifiDevice {

    private static final String TAG = "AoA-" + WifiDevice.class.getSimpleName();

    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    private CallbackHandler callbackHandler;
    private String currentSSID;

    /**
     * Constructor
     *
     * @param context
     * @param appCallbackHandler
     */
    public WifiDevice(Context context, CallbackHandler appCallbackHandler) {
        callbackHandler = appCallbackHandler;

        // Initialize
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get Wifi Info
        currentSSID = getCurrentSSID();

        Log.i(TAG, "CurrentSSID: " + currentSSID);

        // Wifi Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        context.registerReceiver(new wifiReceiver(), filter);
    }

    public interface CallbackHandler {
        public void onSSIDChange(String ssid);
    }

    /**
     * Wifi Receiver
     */
    private class wifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Get Status
            String action = intent.getAction();

            Log.i(TAG, "Receive: Action: " + action);

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                String newSSID = getCurrentSSID();

                if (currentSSID.equals(newSSID)) {
                    return;
                }

                Log.i(TAG, "Receive: OldSSID: " + currentSSID);
                Log.i(TAG, "Receive: NewSSID: " + newSSID);

                // Assign new SSID
                currentSSID = newSSID;

                // Callback
                callbackHandler.onSSIDChange(currentSSID);
            }
        }
    }

    /**
     * Get Current SSID
     *
     * @return
     */
    public String getCurrentSSID() {

        try {
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!networkInfo.isAvailable()) {
                return "";
            }
        } catch (NullPointerException e) {
            return "";
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String currentSSID = wifiInfo.getSSID().replace("\"", "");

        return currentSSID;
    }

    /**
     * Connect AP
     *
     * @param ssid
     * @param pass
     * @param security
     */
    public void connectAP(String ssid, String pass, String security) {

        // New Wifi Config
        WifiConfiguration newConf = new WifiConfiguration();

        newConf.SSID = "\"" + ssid + "\"";

        if ("wpa".equals(security)) {
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

                Log.i(TAG, "Connect: DisableSSID: " + currentConf.SSID.replace("\"", ""));

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

        // Disconnect Wifi Connection
        wifiManager.disconnect();

        Log.i(TAG, "Connect: EnableSSID: " + newConf.SSID.replace("\"", ""));

        wifiManager.enableNetwork(networkId, true);
        wifiManager.saveConfiguration();

        // Reconnect Wifi Connection
        wifiManager.reconnect();
    }
}