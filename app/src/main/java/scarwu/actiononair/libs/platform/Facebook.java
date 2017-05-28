/**
 * Facebook - Account
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs.platform;

// 3rd-Party Libs
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class Facebook {

    public Account account;
    public LiveStream liveStream;

    public Facebook() {

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        account = new Account();
        liveStream = new LiveStream();
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