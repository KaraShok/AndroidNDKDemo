package com.karashok.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.karashok.opengldemo.record.VideoRecorder;
import com.karashok.opengldemo.widget.OpenGLView;
import com.karashok.opengldemo.widget.RecordButton;

public class OpenGLActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);

        OpenGLView glView = findViewById(R.id.opengl_view);
        glView.setOnRecordFinishListener(new VideoRecorder.OnRecordFinishListener() {
            @Override
            public void onRecordFinish(String path) {
                Log.d("TestRecordFinish", "onRecordFinish: " + path);
//                Intent intent = new Intent(OpenGLActivity.this, SoulActivity.class);
//                intent.putExtra("path",path);
//                startActivity(intent);
            }
        });
        glView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glView.switchCamera();
            }
        });

        RecordButton recordButton = findViewById(R.id.btn_record);
        recordButton.setRecordCallback(new RecordButton.OnRecordCallback() {
            @Override
            public void onStart() {
                glView.startRecord();
            }

            @Override
            public void onStop() {
                glView.stopRecord();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.rg_speed);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_extra_slow) {
                    glView.setSpeed(OpenGLView.Speed.MODE_EXTRA_SLOW);
                } else if (checkedId == R.id.rb_slow) {
                    glView.setSpeed(OpenGLView.Speed.MODE_SLOW);
                } else if (checkedId == R.id.rb_normal) {
                    glView.setSpeed(OpenGLView.Speed.MODE_NORMAL);
                } else if (checkedId == R.id.rb_fast) {
                    glView.setSpeed(OpenGLView.Speed.MODE_FAST);
                } else if (checkedId == R.id.rb_extra_fast) {
                    glView.setSpeed(OpenGLView.Speed.MODE_EXTRA_FAST);
                }
            }
        });
    }

}