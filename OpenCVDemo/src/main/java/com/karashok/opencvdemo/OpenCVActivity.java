package com.karashok.opencvdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OpenCVActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private String modelPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv);

        SurfaceView sv = findViewById(R.id.open_cv_sv);
        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width,
                                       int height) {
                setSurface(holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
        cameraHelper = new CameraHelper(cameraId);
        cameraHelper.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                postData(data,CameraHelper.WIDTH,CameraHelper.HEIGHT,cameraId);
            }
        });

        modelPath = Utils.copyAssetsToSdcard(this,"lbpcascade_frontalface.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        init(modelPath);
        cameraHelper.startPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        release();
        cameraHelper.stopPreview();
    }

    /**
     * 初始化追踪器
     * @param model
     */
    native void init(String model);

    /**
     * 设置画布
     * @param surface
     */
    native void setSurface(Surface surface);

    /**
     * 处理摄像头数据
     * @param data
     * @param w
     * @param h
     * @param cameraId
     */
    native void postData(byte[] data, int w, int h, int cameraId);

    native void release();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            cameraHelper.switchCamera();
            cameraId = cameraHelper.getCameraId();
        }
        return super.onTouchEvent(event);
    }
}