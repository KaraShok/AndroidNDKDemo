package com.karashok.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;

import com.karashok.opengldemo.soul.SoulView;

public class SoulActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soul);

        String path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)){
            finish();
        }

        SoulView soulView = findViewById(R.id.soul_view);
        soulView.setDataSource(path);
        soulView.startPlay();
    }
}