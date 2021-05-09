package com.example.assignment1mc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment1mc.db.DbHelper;

import java.io.File;
import java.util.List;

import permissions.OnPermission;
import permissions.Permission;
import permissions.XXPermissions;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_VIDEO_CAPTURE = 101;
    private int RECORD_VIDEO_TIME = 45;

    private SensorManager accelerationManage;
    private Sensor senseAcceleration;
    private SensorEventListener mySensorEventListener;

    int index = 0;
    float[] accelerationValuesX = new float[451];
    float[] accelerationValuesY = new float[451];
    float[] accelerationValuesZ = new float[451];

    private DbHelper dbHelper;

    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button symptomButton = findViewById(R.id.btnsymptoms);
        symptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AssignmentInfoActivity.class);
                startActivity(intent);
            }

        });

        Button bt1 = findViewById(R.id.button);
        if (!hasCamera()) {
            bt1.setEnabled(false);
        }
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMyPermissions();
            }
        });

        final TextView mainTextView = findViewById(R.id.textViewMain);

        dbHelper = DbHelper.init(this);

        Button buttonHR = findViewById(R.id.buttonMeasureHR);
        buttonHR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myAsyncTask != null) {
                    myAsyncTask.cancel(true);
                    myAsyncTask = null;
                }
                myAsyncTask = new MyAsyncTask(MainActivity.this);
                myAsyncTask.setRequestResultCallback(new MyAsyncTask.RequestResultCallback() {
                    @Override
                    public void onCallback(Float result) {
                        Log.i(TAG, "result: " + result);

                        dbHelper.addHR((int) result.floatValue());

                        Log.d(TAG, "HEART_RATE= " + result);

                        mainTextView.setText("HEART_RATE = " + result + " is added to the database");
                    }
                });
                myAsyncTask.execute();
            }
        });

        Button buttonRR = findViewById(R.id.buttonRR);
        buttonRR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rr = 0;
                try {
                    rr = callRespRate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dbHelper.addRR((int) rr);

                Log.d(TAG, "RESPIRATORY_RATE = " + rr);

                mainTextView.setText("RESPIRATORY_RATE = " + rr + " is added to the database");

                dbHelper.close();
            }
        });
    }

    private void requestMyPermissions() {
        XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
                            startRecording();
                            initAccelerometer();
                        } else {
                            makeText(getApplicationContext(), "Got Permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            makeText(getApplicationContext(), "Did not get Permissions", Toast.LENGTH_SHORT).show();
                            XXPermissions.gotoPermissionSettings(MainActivity.this);
                        } else {
                            makeText(getApplicationContext(), "No Permissions", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void startRecording() {
        File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FingertipVideo.mp4");

        Log.d(TAG, "ACTION_VIDEO_CAPTURED starting to initialize");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Log.d(TAG, "ACTION_VIDEO_CAPTURED initialized");
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, RECORD_VIDEO_TIME);
        Log.d(TAG, "EXTRA_DURATION_LIMIT");

        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.assignment1mc.provider", mediaFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE);
    }

    private void initAccelerometer() {
        Log.d(TAG, "onCreate: Initializing Sensor Services");

        accelerationManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAcceleration = accelerationManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mySensorEventListener == null) {
            mySensorEventListener = new MySensorEventListener();
        }
        accelerationManage.registerListener(mySensorEventListener, senseAcceleration, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d(TAG, "onCreate: Registered accelerometer listener");
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                makeText(this, "Video has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                makeText(this, "Video canceled", Toast.LENGTH_LONG).show();
            } else {
                makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
            }

            accelerationManage.unregisterListener(mySensorEventListener);
        }
    }

    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " ,Y: " + event.values[1] + " ,Z: " + event.values[2]);

            Sensor mySensor = event.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                index++;
                accelerationValuesX[index] = event.values[0];
                accelerationValuesY[index] = event.values[1];
                accelerationValuesZ[index] = event.values[2];
                if (index >= 127) {
                    index = 0;
                    accelerationManage.unregisterListener(this);
                    callFallRecognition();
                    accelerationManage.registerListener(this, senseAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public float callRespRate() {
        float[] diffArray = new float[450];
        for (int i = 0; i < 450; i++) {
            diffArray[i] = Math.abs(accelerationValuesZ[i + 1] - accelerationValuesZ[i]);
        }
        // calculate number of zeros in diffArray and divide that number by 2.
        float epsilon;
        epsilon = (float) 0.05;
        int newPeak = 0;
        for (int i = 1; i < 450; i++) {
            if (diffArray[i] < epsilon && diffArray[i - 1] > epsilon) {
                newPeak = newPeak + 1;
            }
        }

        return (float) (60 * newPeak / 45);
    }

    public void callFallRecognition() {
        float prev;
        float curr;
        prev = 10;
        for (int i = 11; i < 128; i++) {
            curr = accelerationValuesZ[i];
            if (Math.abs(prev - curr) > 10) {
                makeText(this, "Fall detected", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAsyncTask.cancel(true);
    }
}
