/**
 * ControlPanel Activity
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.Switch;

// Custom Libs
import scarwu.actiononair.libs.DBHelper;
import scarwu.actiononair.libs.FontManager;
import scarwu.actiononair.sns.Facebook;
import scarwu.actiononair.sns.Google;
import scarwu.actiononair.cameras.SonyActionCam;
import scarwu.actiononair.cameras.sony.StreamSurfaceView;

public class ControlPanelActivity extends AppCompatActivity {

    private static final String TAG = "AoA-" + ControlPanelActivity.class.getSimpleName();

    private Activity appActivity;
    private Context appContext;

    // Widgets
    private Switch micSoundSrcSwitch;
    private Switch camSoundSrcSwitch;
    private SurfaceView liveView;

    // Flags
    private boolean isLive = false;
    private String snsProvider = null;
    private String cameraProvider = null;

    private Facebook snsFacebook;
    private Google snsGoogle;

    private SonyActionCam sonyActionCam;

    private StreamSurfaceView streamSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_control_panel);

        // Get Application Objects
        appActivity = this;
        appContext = getApplicationContext();

        // Get Intent Extra
        snsProvider = getIntent().getExtras().getString("snsProvider");
        cameraProvider = getIntent().getExtras().getString("cameraProvider");

        // Social Network
        if ("facebook".equals(snsProvider)) {
            snsFacebook = new Facebook();
        } else if ("google".equals(snsProvider)) {
            snsGoogle = new Google();
        } else {
            Log.i("AoA-Activity", "Switch");

            finish();
        }

        // Camera
        if ("sony".equals(cameraProvider)) {
            sonyActionCam = new SonyActionCam(appContext, new SonyActionCam.CallbackHandler() {

                @Override
                public void onFoundDevice(String ipAdress) {
                    Log.i("AoA-Activity", "Switch");
                }
            });
        } else {
            finish();
        }

        // Initialize Widgets
        initWidgets();

        // Discover Device
        sonyActionCam.discover();
    }

    /**
     * Init Widgets
     */
    private void initWidgets() {

        final Button startAndStop = (Button) findViewById(R.id.startAndStop);

        startAndStop.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
        startAndStop.setText(R.string.icon_start);
        startAndStop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isLive) {
                    if ("facebook".equals(snsProvider)) {
                        snsFacebook.liveStream.stop();
                    } else if ("google".equals(snsProvider)) {
                        snsGoogle.liveStream.stop();
                    }

                    // Set Start Icon
                    startAndStop.setText(R.string.icon_start);

                    isLive = false;
                } else {
                    if ("facebook".equals(snsProvider)) {
                        snsFacebook.liveStream.start();
                    } else if ("google".equals(snsProvider)) {
                        snsGoogle.liveStream.start();
                    }

                    // Set Stop Icon
                    startAndStop.setText(R.string.icon_stop);

                    isLive = true;
                }
            }
        });

        // Live View
        liveView = (SurfaceView) findViewById(R.id.liveView);

        micSoundSrcSwitch = (Switch) findViewById(R.id.micSwitch);
        micSoundSrcSwitch.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));

        camSoundSrcSwitch = (Switch) findViewById(R.id.camSwitch);
        camSoundSrcSwitch.setTypeface(FontManager.getTypeface(appContext, FontManager.FONTAWESOME));
    }
}
