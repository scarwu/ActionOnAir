/**
 * Sony Remote API Caller
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
import java.util.List;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import scarwu.actiononair.cameras.sony.RemoteDevice.ApiService;
import scarwu.actiononair.libs.HttpClient;

/**
 * Simple Camera Remote API wrapper class. (JSON based API <--> Java API)
 */
public class RemoteApiCaller {

    private static final String TAG = "AoA-" + RemoteApiCaller.class.getSimpleName();
    private static final boolean FULL_LOG = true;

    private RemoteDevice currentDevice;

    private int requestId;

    /**
     * Constructor.
     *
     * @param device server device of Remote API
     */
    public RemoteApiCaller(RemoteDevice device) {
        currentDevice = device;
        requestId = 1;
    }

    /**
     * Retrieves Action List URL from Server information.
     *
     * @param service
     * @return
     * @throws IOException
     */
    private String findActionListUrl(String service) throws IOException {
        List<ApiService> services = currentDevice.getApiServices();

        for (ApiService apiService : services) {
            if (apiService.getName().equals(service)) {
                return apiService.getActionListUrl();
            }
        }

        throw new IOException("actionUrl not found. service : " + service);
    }

    /**
     * Request ID. Counted up after calling.
     *
     * @return
     */
    private int id() {
        return requestId++;
    }

    // Output a log line.
    private void log(String msg) {
        if (FULL_LOG) {
            Log.d(TAG, msg);
        }
    }

    // Camera Service APIs

    /**
     * Sender
     *
     * @param reqJson
     * @param service
     * @param timeout
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    private JSONObject sender(String service, JSONObject reqJson, int timeout) throws IOException {
        try {
            String url = findActionListUrl(service) + "/" + service;

            log("Request:  " + reqJson.toString());

            String resJson = (0 != timeout)
                ? HttpClient.httpPost(url, reqJson.toString(), timeout)
                : HttpClient.httpPost(url, reqJson.toString());

            log("Response: " + resJson);

            return new JSONObject(resJson);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getAvailableApiList API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableApiList",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getAvailableApiList() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getAvailableApiList")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getApplicationInfo API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "getApplicationInfo",
     *   "params": [""],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getApplicationInfo() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getApplicationInfo")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getShootMode API
     *
     * <pre>
     * {
     *   "method": "getShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getShootMode() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getShootMode")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls setShootMode API
     *
     * <pre>
     * {
     *   "method": "setShootMode",
     *   "params": ["still"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param shootMode shoot mode (ex. "still")
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject setShootMode(String shootMode) throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "setShootMode")
                .put("params", new JSONArray().put(shootMode))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getAvailableShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getAvailableShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getAvailableShootMode() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getAvailableShootMode")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getSupportedShootMode API to the target server. Request JSON data
     * is such like as below.
     *
     * <pre>
     * {
     *   "method": "getSupportedShootMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getSupportedShootMode() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getSupportedShootMode")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls startLiveview API
     *
     * <pre>
     * {
     *   "method": "startLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject startLiveview() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "startLiveview")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls stopLiveview API
     *
     * <pre>
     * {
     *   "method": "stopLiveview",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject stopLiveview() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "stopLiveview")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls startRecMode API
     *
     * <pre>
     * {
     *   "method": "startRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject startRecMode() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "startRecMode")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls actTakePicture API
     *
     * <pre>
     * {
     *   "method": "actTakePicture",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException
     */
    public JSONObject actTakePicture() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "actTakePicture")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls startMovieRec API
     *
     * <pre>
     * {
     *   "method": "startMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject startMovieRec() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "startMovieRec")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls stopMovieRec API
     *
     * <pre>
     * {
     *   "method": "stopMovieRec",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject stopMovieRec() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "stopMovieRec")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls actZoom API
     *
     * <pre>
     * {
     *   "method": "actZoom",
     *   "params": ["in","stop"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param direction direction of zoom ("in" or "out")
     * @param movement zoom movement ("start", "stop", or "1shot")
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject actZoom(String direction, String movement) throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "actZoom")
                .put("params", new JSONArray().put(direction).put(movement))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getEvent API to the target server. Request JSON data is such like
     * as below.
     *
     * <pre>
     * {
     *   "method": "getEvent",
     *   "params": [true],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param longPollingFlag true means long polling request.
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getEvent(boolean longPollingFlag) throws IOException {
        String service = "camera";
        int timeout = (longPollingFlag) ? 20000 : 8000; // msec

        try {
            JSONObject json = new JSONObject().put("method", "getEvent")
                .put("params", new JSONArray().put(longPollingFlag))
                .put("version", "1.0").put("id", id());

            return sender(service, json, timeout);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls setCameraFunction API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "setCameraFunction",
     *   "params": ["Remote Shooting"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param cameraFunction camera function to set
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject setCameraFunction(String cameraFunction) throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "setCameraFunction")
                .put("params", new JSONArray().put(cameraFunction))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getMethodTypes API of Camera service to the target server. Request
     * JSON data is such like as below.
     *
     * <pre>
     * {
     *   "method": "getMethodTypes",
     *   "params": ["1.0"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getCameraMethodTypes() throws IOException {
        String service = "camera";

        try {
            JSONObject json = new JSONObject().put("method", "getMethodTypes")
                .put("params", new JSONArray().put(""))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    // Avcontent APIs

    /**
     * Calls getMethodTypes API of AvContent service to the target server.
     * Request JSON data is such like as below.
     *
     * <pre>
     * {
     *   "method": "getMethodTypes",
     *   "params": ["1.0"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getAvcontentMethodTypes() throws IOException {
        String service = "avContent";

        try {
            JSONObject json = new JSONObject().put("method", "getMethodTypes")
                .put("params", new JSONArray().put(""))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getSchemeList API
     *
     * <pre>
     * {
     *   "method": "getSchemeList",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getSchemeList() throws IOException {
        String service = "avContent";

        try {
            JSONObject json = new JSONObject().put("method", "getSchemeList")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getSourceList API
     *
     * <pre>
     * {
     *   "method": "getSourceList",
     *   "params": [{
     *      "scheme": "storage"
     *      }],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param scheme target scheme to get source
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getSourceList(String scheme) throws IOException {
        String service = "avContent";

        try {
            JSONObject params = new JSONObject()
                .put("scheme", scheme);

            JSONObject json = new JSONObject().put("method", "getSourceList")
                .put("params", new JSONArray().put(0, params))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls getContentList API
     *
     * <pre>
     * {
     *   "method": "getContentList",
     *   "params": [{
     *      "sort" : "ascending"
     *      "view": "date"
     *      "uri": "storage:memoryCard1"
     *      }],
     *   "id": 2,
     *   "version": "1.3"
     * }
     * </pre>
     *
     * @param params request JSON parameter of "params" object.
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject getContentList(JSONArray params) throws IOException {
        String service = "avContent";

        try {
            JSONObject json = new JSONObject().put("method", "getContentList")
                .put("params", params)
                .put("version", "1.3").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls setStreamingContent API to the target server. Request JSON data is
     * such like as below.
     *
     * <pre>
     * {
     *   "method": "setStreamingContent",
     *   "params": [
     *      "remotePlayType" : "simpleStreaming"
     *      "uri": "image:content?contentId=01006"
     *      ],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @param uri streaming contents uri
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject setStreamingContent(String uri) throws IOException {
        String service = "avContent";

        try {
            JSONObject params = new JSONObject()
                .put("remotePlayType", "simpleStreaming")
                .put("uri", uri);

            JSONObject json = new JSONObject().put("method", "setStreamingContent")
                .put("params", new JSONArray().put(0, params))
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls startStreaming API
     *
     * <pre>
     * {
     *   "method": "startStreaming",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject startStreaming() throws IOException {
        String service = "avContent";

        try {
            JSONObject json = new JSONObject().put("method", "startStreaming")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls stopStreaming API
     *
     * <pre>
     * {
     *   "method": "stopStreaming",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     *
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this Exception.
     */
    public JSONObject stopStreaming() throws IOException {
        String service = "avContent";

        try {
            JSONObject json = new JSONObject().put("method", "stopStreaming")
                .put("params", new JSONArray())
                .put("version", "1.0").put("id", id());

            return sender(service, json, 0);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    // static method

    /**
     * Parse JSON and return whether it has error or not.
     *
     * @param replyJson JSON object to check
     * @return return true if JSON has error. otherwise return false.
     */
    public static boolean isErrorReply(JSONObject replyJson) {
        boolean hasError = (replyJson != null && replyJson.has("error"));

        return hasError;
    }
}
