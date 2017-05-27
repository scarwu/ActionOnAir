package scarwu.actiononair;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;


// Custom Libs
import scarwu.actiononair.libs.platform.facebook.*;
import scarwu.actiononair.libs.platform.google.*;
import scarwu.actiononair.libs.camera.sony.ActionCam;
import scarwu.actiononair.libs.NFCReader;

public class MainActivity extends AppCompatActivity {

    private Button snsFBAuth;
    private Button snsFBLive;
    private Button snsGoogleAuth;
    private Button snsGoogleLive;
    private Button cameraNFCReader;
    private ListView cameraList;

    private NFCReader nfcReader;

    private boolean isFBAuth = false;
    private boolean isGoogleAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        nfcReader = new NFCReader();

        // Initailize View Units
        initViewUnits();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    private void initViewUnits() {

        // Facebook Control
        snsFBAuth = (Button) findViewById(R.id.snsFBAuth);
        snsFBAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isFBAuth) {
                    // disconnect
                } else {
                    // connect
                }
            }
        });

        snsFBLive = (Button) findViewById(R.id.snsFBLive);
        snsFBLive.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isFBAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Facebook Account", Toast.LENGTH_SHORT).show();

                    return;
                }
            }
        });

        // Google Control
        snsGoogleAuth = (Button) findViewById(R.id.snsGoogleAuth);
        snsGoogleAuth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (isGoogleAuth) {
                    // disconnect
                } else {
                    // connect
                }
            }
        });

        snsGoogleLive = (Button) findViewById(R.id.snsGoogleLive);
        snsGoogleLive.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!isGoogleAuth) {
                    Toast.makeText(view.getContext(), "Please Connect Your Google Account", Toast.LENGTH_SHORT).show();

                    return;
                }
            }
        });

        // Camera Control
        cameraNFCReader = (Button) findViewById(R.id.cameraNFCReader);
        cameraNFCReader.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

            }
        });

        cameraList = (ListView) findViewById(R.id.cameraList);
    }
}
