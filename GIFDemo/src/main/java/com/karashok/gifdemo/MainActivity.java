package com.karashok.gifdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("gifdemo");
    }

    private ImageView iv;
    private Bitmap bitmap;
    private GifHandler gifHandler = new GifHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gifHandler.loadPath(new File(Environment.getExternalStorageDirectory(),"demo.gif").getAbsolutePath());
                        bitmap = Bitmap.createBitmap(gifHandler.getWidth(),gifHandler.getHeight(),Bitmap.Config.ARGB_8888);
//                        int dealy = gifHandler.updateFrame(bitmap);
//                        handler.sendEmptyMessageDelayed(1,dealy);
                        showBitmap();
                    }
                });
        iv = findViewById(R.id.main_iv);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            showBitmap();
        }
    };

    private void showBitmap() {
        int dealy = gifHandler.updateFrame(bitmap);
        iv.setImageBitmap(bitmap);
        handler.sendEmptyMessageDelayed(1,dealy);
    }
}