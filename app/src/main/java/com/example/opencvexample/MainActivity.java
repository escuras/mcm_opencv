package com.example.opencvexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkPermissions();

    }

    public void colorDetect(View view) {
        Intent activity = new Intent(this, ColorDetectActivity.class);
        startActivity(activity);
    }

    public void faceDetect(View view) {
        Intent activity = new Intent(this, FaceDetectActivity.class);
        startActivity(activity);
    }

    public void effectsChoose(View view) {
        Intent activity = new Intent(this, NativeOptionActivity.class);
        startActivity(activity);
    }

    public void imageChoose(View view) {
        Intent activity = new Intent(this, ImageActivity.class);
        startActivity(activity);
    }

    public void coinsChoose(View view) {
        Intent activity = new Intent(this, CoinActivity.class);
        startActivity(activity);
    }

    public void handsDetect(View view) {
        Intent activity = new Intent(this, HandsDetectActivity.class);
        startActivity(activity);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE },1);
        }
    }
}
