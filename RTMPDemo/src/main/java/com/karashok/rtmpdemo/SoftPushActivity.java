package com.karashok.rtmpdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;

import com.karashok.rtmpdemo.channel.LivePusher;

public class SoftPushActivity extends AppCompatActivity {

    private SurfaceView sv;
    private LivePusher pusher;
    private static final String URL = "rtmp://49.232.167.72:1935/live/jason";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_push);
        sv = findViewById(R.id.soft_sv);

        findViewById(R.id.soft_switch_btn)
                .setOnClickListener(clickListener);
        findViewById(R.id.soft_start_btn)
                .setOnClickListener(clickListener);
        findViewById(R.id.soft_stop_btn)
                .setOnClickListener(clickListener);

        pusher = new LivePusher(this,800,480,800_000,10, Camera.CameraInfo.CAMERA_FACING_BACK);
        pusher.setPreviewDisplay(sv.getHolder());
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int vId = v.getId();
            if (vId == R.id.soft_switch_btn) {
                pusher.switchCamera();
            } else if (vId == R.id.soft_start_btn) {
                pusher.startLive(URL);
            } else if (vId == R.id.soft_stop_btn) {
                pusher.stopLive();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pusher.release();
    }
}