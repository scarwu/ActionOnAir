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

// Custom Libs
import scarwu.actiononair.libs.DBHelper;
import scarwu.actiononair.libs.FontManager;
import scarwu.actiononair.libs.sns.Facebook;
import scarwu.actiononair.libs.sns.Google;
import scarwu.actiononair.libs.camera.SonyActionCam;

public class ControlPanelActivity extends AppCompatActivity {

    // Widgets
    private SurfaceView liveView;

    // Flags
    private boolean isLive = false;
    private String sns = null;
    private String provider = null;

    private Facebook snsFB;
    private Google snsGoogle;

    private SonyActionCam sonyActionCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_control_panel);

        // Get Intent Extra
        sns = getIntent().getExtras().getString("sns");
        provider = getIntent().getExtras().getString("provider");

        // Social Network
        if ("fb".equals(sns)) {
            snsFB = new Facebook();
        } else if ("google".equals(sns)) {
            snsGoogle = new Google();
        } else {
            finish();
        }

        // Camera
        if ("sony".equals(sns)) {
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
        startAndStop.setText("\uf00d");
        startAndStop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isLive) {
                    if ("fb".equals(sns)) {
                        snsFB.liveStream.stop();
                    } else if ("google".equals(sns)) {
                        snsGoogle.liveStream.stop();
                    }

                    isLive = false;

                    // Set Start Icon
                    startAndStop.setText("\uf00d");
                } else {
                    if ("fb".equals(sns)) {
                        snsFB.liveStream.start();
                    } else if ("google".equals(sns)) {
                        snsGoogle.liveStream.start();
                    }

                    isLive = true;

                    // Set Stop Icon
                    startAndStop.setText("\uf04d");
                }
            }
        });

        // Live View
        liveView = (SurfaceView) findViewById(R.id.liveView);
    }
}
