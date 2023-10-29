package com.karashok.rtmpdemo.channel;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

/**
 * @author karashok
 * @since 02-28-2023
 */
public class VideoChannel implements Camera.PreviewCallback,CameraHelper.OnChangedSizeListener {

    private LivePusher pusher;
    private CameraHelper cameraHelper;
    private int mBitrate;
    private int mFps;
    private boolean isLiving;

    public VideoChannel(LivePusher lp, Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        pusher = lp;
        mBitrate = bitrate;
        mFps = fps;
        cameraHelper = new CameraHelper(activity, height, width, cameraId);

        // 1、让camerahelper的
        cameraHelper.setPreviewCallback(this);

        // 2、回调 真实的摄像头数据宽、高
        cameraHelper.setOnChangedSizeListener(this);
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        cameraHelper.setPreviewDisplay(holder);
    }

    public void switchCamera() {
        cameraHelper.switchCamera();
    }

    public void startLive() {
        isLiving = true;
    }

    public void stopLive() {
        isLiving = false;
    }

    public void release() {
        cameraHelper.release();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isLiving) {
            pusher.nativePushVideo(data);
        }
    }

    @Override
    public void onChanged(int w, int h) {
        pusher.nativeSetVideoEncInfo(w,h,mFps,mBitrate);
    }
}
