package com.karashok.opengldemo.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.karashok.opengldemo.record.VideoRecorder;

/**
 * @author karashok
 * @since 04-17-2023
 */
public class OpenGLView extends GLSurfaceView {

    //默认正常速度
    private Speed mSpeed = Speed.MODE_NORMAL;

    public enum Speed {
        MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST
    }

    private OpenGLRenderer mRenderer;

    public OpenGLView(Context context) {
        this(context,null);
    }

    public OpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRenderer = new OpenGLRenderer(this);
        // 设置 EGL 版本
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);

        // 按需渲染，请求一次 GLThread 回调一次 onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mRenderer.onSurfaceDestroyed();
    }

    public void setSpeed(Speed speed){
        mSpeed = speed;
    }

    public void startRecord() {
        float speed = 1.f;
        switch (mSpeed) {
            case MODE_EXTRA_SLOW:
                speed = 0.3f;
                break;
            case MODE_SLOW:
                speed = 0.5f;
                break;
            case MODE_NORMAL:
                speed = 1.0f;
                break;
            case MODE_FAST:
                speed = 1.5f;
                break;
            case MODE_EXTRA_FAST:
                speed = 3.f;
                break;
        }
        mRenderer.startRecord(speed);
    }

    public void stopRecord() {
        mRenderer.stopRecord();
    }

    public void switchCamera() {
        mRenderer.switchCamera();
    }

    public void setOnRecordFinishListener(VideoRecorder.OnRecordFinishListener listener){
        mRenderer.setOnRecordFinishListener(listener);
    }
}
