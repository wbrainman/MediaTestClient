package com.example.mediatestservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MediaTest";
    private IMediaTestService iMediaTestService;
    private static final String MEDIA_TEST_SERVICE_PACKEG_NAME =
            "com.example.mediatestservice";
    private static final String MEDIA_TEST_SERVICE_CLASS_NAME =
            "com.example.mediatestservice.MediaTestService";

    private String[] permissions = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO };
    private List<String> mPermissionList = new ArrayList<>();

    private Button btn_start, btn_stop;
    private Chronometer chronometer;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            iMediaTestService = IMediaTestService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            iMediaTestService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");
        checkPermission();
        bindService();
    }

    private void checkPermission() {
        Log.d(TAG, "checkPermission: ");
        mPermissionList.clear();
        for(String permission : permissions) {
            Log.d(TAG, "initPermission: checking permission : " + permission);
            if(ContextCompat.checkSelfPermission(
                    MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "initPermission: permission : " + permission + "added");
                mPermissionList.add(permission);
            }
        }
        if(mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        else {
            init();
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: ");

        boolean denied = false;
        switch (requestCode) {
            case 1:
                for(int i = 0; i < grantResults.length; i ++) {
                    if(grantResults[i] == -1) {
                        denied = true;
                    }
                }
                if(denied) {
                    Toast.makeText(
                            MainActivity.this,
                            "permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    init();
                }
                break;
            default:
                break;
        }
    }

    private void init() {
        Log.d(TAG, "init: ");
        btn_start = (Button) findViewById(R.id.start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                int hour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
                chronometer.setFormat("0" + String.valueOf(hour)+":%s");
                chronometer.start();
                try {
                    iMediaTestService.startRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                if(iMediaTestService.isRecording()) {
//                    btn_start.setEnabled(false);
//                    btn_stop.setEnabled(true);
//                }

            }
        });

        btn_stop = (Button) findViewById(R.id.stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
                try {
                    iMediaTestService.stopRecord();
//                    if(iMediaTestService.isRecording()) {
//                        btn_start.setEnabled(true);
//                        btn_stop.setEnabled(false);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClassName( MEDIA_TEST_SERVICE_PACKEG_NAME, MEDIA_TEST_SERVICE_CLASS_NAME);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }
}