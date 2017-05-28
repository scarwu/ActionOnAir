/**
 * Facebook - Account
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs.sns;

// 3rd-Party Libs
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class Facebook {

    private static boolean isInit = false;

    public static Account account;
    public static LiveStream liveStream;

    public Facebook() {

        if (isInit) {
            return;
        }

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

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