package com.karashok.rtmpdemo.channel;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author karashok
 * @since 02-28-2023
 */
public class LivePusher {

    static {
        System.loadLibrary("rtmpdemo");
    }

    private AudioChannel audioChannel;
    private VideoChannel videoChannel;

    public LivePusher(Activity activity,int width, int height, int bitrate,
                      int fps, int cameraId) {
        nativeInit();
        videoChannel = new VideoChannel(this,activity,width,height,bitrate,fps,cameraId);
        audioChannel = new AudioChannel(this);
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        videoChannel.setPreviewDisplay(holder);
    }

    public void switchCamera() {
        videoChannel.switchCamera();
    }

    public void startLive(String path) {
        nativeStart(path);
        audioChannel.startLive();
        videoChannel.startLive();
    }

    public void stopLive() {
        audioChannel.stopLive();
        videoChannel.stopLive();
        nativeStop();
    }

    public void release() {
        audioChannel.release();
        videoChannel.release();
        nativeRelease();
    }

    public native void nativeInit();

    public native void nativeStart(String path);

    public native void nativeSetVideoEncInfo(int width, int height, int fps, int bitrate);

    public native void nativeSetAudioEncInfo(int sampleRateInHz, int channels);

    public native void nativePushVideo(byte[] data);

    public native void nativeStop();

    public native void nativeRelease();

    public native int nativeGetInputSamples();

    public native void nativePushAudio(byte[] data);
}
