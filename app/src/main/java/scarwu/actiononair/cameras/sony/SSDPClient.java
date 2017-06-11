/**
 * Sony SSDP Client
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

/**
 * Copyright 2014 Sony Corporation
 */

package scarwu.actiononair.cameras.sony;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import scarwu.actiononair.cameras.sony.RemoteDevice;

public class SSDPClient {

    private static final String TAG = "AoA-" + SSDPClient.class.getSimpleName();
    private static final String SSDP_ADDR = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final int SSDP_RECEIVE_TIMEOUT = 10000; // msec

    private boolean isSearch = false;

    public SSDPClient() {

    }

    public interface CallbackHandler {
        void onDeviceFound(RemoteDevice device);
        void onFinish();
        void onError();
    }

    /**
     * Search API server device.
     *
     * @param handler result handler
     * @return true: start successfully, false: already searching now
     */
    public synchronized boolean startSearch(final CallbackHandler handler) {
        if (isSearch) {
            Log.w(TAG, "Search: Already Searching");
            return false;
        }

        if (handler == null) {
            throw new NullPointerException("Handler is null");
        }

        Log.i(TAG, "Search: Start.");

        final String ssdpRequest =
            "M-SEARCH * HTTP/1.1\r\n"
                + String.format("HOST: %s:%d\r\n", SSDP_ADDR, SSDP_PORT)
                + "MAN: \"ssdp:discover\"\r\n"
                + "MX: 1\r\n"
                + "ST: urn:schemas-sony-com:service:ScalarWebAPI:1\r\n\r\n";
        final byte[] sendData = ssdpRequest.getBytes();

        new Thread() {

            @Override
            public void run() {

                // Send Datagram packets
                DatagramSocket socket = null;
                DatagramPacket resPacket = null;
                DatagramPacket reqPacket = null;

                try {
                    InetSocketAddress iAddress = new InetSocketAddress(SSDP_ADDR, SSDP_PORT);

                    socket = new DatagramSocket();
                    reqPacket = new DatagramPacket(sendData, sendData.length, iAddress);

                    // send 3 times
                    Log.i(TAG, "Search: Send Datagram Packets");

                    socket.send(reqPacket);
                    Thread.sleep(100);
                    socket.send(reqPacket);
                    Thread.sleep(100);
                    socket.send(reqPacket);
                } catch (SocketException e) {
                    Log.e(TAG, "Search: SocketException:", e);

                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }

                    handler.onError();

                    return;
                }catch (IOException e) {
                    Log.e(TAG, "Search: IOException:", e);

                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }

                    handler.onError();

                    return;
                }  catch (InterruptedException e) {
                    // do nothing.
                    Log.d(TAG, "Search: InterruptedException:", e);
                }

                // Receive reply packets
                isSearch = true;
                long startTime = System.currentTimeMillis();
                List<String> foundDevices = new ArrayList<String>();
                byte[] array = new byte[1024];
                RemoteDevice device = null;

                try {
                    while (isSearch) {
                        resPacket = new DatagramPacket(array, array.length);
                        socket.setSoTimeout(SSDP_RECEIVE_TIMEOUT);
                        socket.receive(resPacket);

                        String ssdpReplyMessage = new String(resPacket.getData(), 0, resPacket.getLength(), "UTF-8");
                        String ddUsn = findParameterValue(ssdpReplyMessage, "USN");
                        Log.i(TAG, ssdpReplyMessage);
                        /*
                         * There is possibility to receive multiple packets from
                         * a individual server.
                         */
                        if (!foundDevices.contains(ddUsn)) {
                            String ddLocation = findParameterValue(ssdpReplyMessage, "LOCATION");
                            foundDevices.add(ddUsn);

                            // Fetch Device Description XML and parse it.
                            device = RemoteDevice.fetch(ddLocation);
                            // Note that it's a irresponsible rule
                            // for the sample application.
                            if (device != null && device.hasApiService("camera")) {
                                handler.onDeviceFound(device);
                            }

                            break;
                        }

                        if (SSDP_RECEIVE_TIMEOUT < System.currentTimeMillis() - startTime) {
                            break;
                        }
                    }
                } catch (InterruptedIOException e) {
                    Log.d(TAG, "Search: InterruptedIOException:", e);

                    if (device == null) {
                        isSearch = false;

                        handler.onError();

                        return;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Search: IOException:", e);

                    isSearch = false;

                    handler.onError();

                    return;
                } finally {
                    Log.d(TAG, "Search: Finish");

                    isSearch = false;

                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                }

                handler.onFinish();
            };
        }.start();

        return true;
    }

    /**
     * Checks whether searching is in progress or not.
     *
     * @return true: now searching, false: otherwise
     */
    public boolean isSearching() {
        return isSearch;
    }

    /**
     * Cancels searching. Note that it cannot stop the operation immediately.
     */
    public void stopSearch() {
        isSearch = false;
    }

    /**
     * Find a value string from message line as below. (ex.)
     * "ST: XXXXX-YYYYY-ZZZZZ" -> "XXXXX-YYYYY-ZZZZZ"
     */
    private static String findParameterValue(String ssdpMessage, String paramName) {
        String name = paramName;

        if (!name.endsWith(":")) {
            name = name + ":";
        }

        int start = ssdpMessage.indexOf(name);
        int end = ssdpMessage.indexOf("\r\n", start);

        if (start != -1 && end != -1) {
            start += name.length();
            String val = ssdpMessage.substring(start, end);

            if (val != null) {
                return val.trim();
            }
        }

        return null;
    }
}
