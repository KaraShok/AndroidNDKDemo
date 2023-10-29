package com.karashok.andfixdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("andfixdemo");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.main_tv);
        tv.setText(stringFromJNI());
        findViewById(R.id.bug_btn)
                .setOnClickListener(clickListener);
        findViewById(R.id.fix_btn)
                .setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int vId = v.getId();
            if (vId == R.id.bug_btn) {
                Caclutor caclutor = new Caclutor();
                caclutor.test(MainActivity.this);
            } else if (vId == R.id.fix_btn) {
                DexManager dexManager = new DexManager(MainActivity.this);
                dexManager.load(FixCaclutor.class);
            }
        }
    };

    public native String stringFromJNI();
}