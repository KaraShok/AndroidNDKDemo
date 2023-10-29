package com.karashok.rtmpdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class HardwarePushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_push);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}