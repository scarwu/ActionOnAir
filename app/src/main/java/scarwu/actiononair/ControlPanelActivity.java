/**
 * ControlPanel Activity
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair;

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
import scarwu.actiononair.libs.sns.Facebook;
import scarwu.actiononair.libs.sns.Google;
import scarwu.actiononair.libs.camera.SonyActionCam;

public class ControlPanelActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_control_panel);

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
            sonyActionCam = new SonyActionCam();
        } else {
            finish();
        }

        // Initialize Widgets
        initWidgets();
    }

    /**
     * Init Widgets
     */
    private void initWidgets() {

        final Button startAndStop = (Button) findViewById(R.id.startAndStop);

        startAndStop.setTypeface(FontManager.getTypeface(ControlPanelActivity.this, FontManager.FONTAWESOME));
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
        micSoundSrcSwitch.setTypeface(FontManager.getTypeface(ControlPanelActivity.this, FontManager.FONTAWESOME));

        camSoundSrcSwitch = (Switch) findViewById(R.id.camSwitch);
        camSoundSrcSwitch.setTypeface(FontManager.getTypeface(ControlPanelActivity.this, FontManager.FONTAWESOME));
    }
}
