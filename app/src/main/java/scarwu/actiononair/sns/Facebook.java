/**
 * Facebook
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.sns;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import scarwu.actiononair.libs.HttpClient;

public class Facebook {

    private static final String TAG = "AoA-" + Facebook.class.getSimpleName();

    private String accessToken;
    private String userId;
    private String videoId;
    private String streamUrl;
    private String secureStreamUrl;

    public Facebook(String accessToken) throws IOException, JSONException {
        this.accessToken = accessToken;

        Log.i(TAG, "AccessToken: " + this.accessToken);
    }

    public void getUserId() throws IOException {
        try {
            String url = "https://graph.facebook.com/v2.9/me"
                + "?access_token=" + this.accessToken + "&fields=id";

            JSONObject json = new JSONObject(HttpClient.httpGet(url));

            this.userId = json.getJSONArray("id").get(0).toString();

            Log.i(TAG, "UserId: " + this.userId);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public void startStreaming() throws IOException {
        try {
            String url = "https://graph.facebook.com/v2.9/" + this.userId + "/live_videos"
                + "?access_token=" + this.accessToken;

            JSONObject json = new JSONObject(HttpClient.httpPost(url));

            this.videoId = json.getJSONArray("id").get(0).toString();
            this.streamUrl = json.getJSONArray("stream_url").get(0).toString();
            this.secureStreamUrl = json.getJSONArray("secure_stream_url").get(0).toString();

            Log.i(TAG, "UserId: " + this.userId);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public void stopStreaming() throws IOException {
        try {
            String url = "https://graph.facebook.com/v2.9/" + this.videoId
                    + "?access_token=" + this.accessToken + "&end_live_video=true";

            JSONObject json = new JSONObject(HttpClient.httpPost(url));
            this.secureStreamUrl = json.getJSONArray("id").get(0).toString();

        } catch (JSONException e) {
            throw new IOException(e);
        }
    }
}
