/**
 * Facebook - Account
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs.sns;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

// 3rd-Party Libs
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.auth.oauth2.StoredCredential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.DataStore;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.util.DateTime;
//import com.google.api.services.samples.youtube.cmdline.Auth;
//import com.google.api.services.youtube.YouTube;
//import com.google.api.services.youtube.model.*;
//import com.google.common.collect.Lists;

public class Google {

    private static boolean isInit = false;

    public static Account account;
    public static LiveStream liveStream;

    public Google() {

        if (isInit) {
            return;
        }

        account = new Account();
        liveStream = new LiveStream();

        isInit = true;
    }

    public class Account {

        public Account() {

        }

        public void connect() {

        }

        public void disconnect() {

        }
    }

    public class LiveStream {

        public LiveStream() {

        }

        public void start() {

        }

        public void stop() {

        }
    }
}