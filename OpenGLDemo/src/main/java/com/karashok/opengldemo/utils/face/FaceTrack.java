package com.karashok.opengldemo.utils.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.karashok.opengldemo.utils.CameraHelper;

/**
 * @author karashok
 * @since 05-20-2023
 */
public class FaceTrack {

    private CameraHelper mCameraHelper;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private long self;
    private FaceData mFaceData;

    public FaceTrack(String model, String seeta, CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
        self = nativeCreate(model,seeta);
        mHandlerThread = new HandlerThread("FaceTrack");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                synchronized (FaceTrack.this) {
                    mFaceData = nativeDetector(self,(byte[]) msg.obj,
                            mCameraHelper.getCameraId(),CameraHelper.WIDTH,CameraHelper.HEIGHT);
                }
            }
        };
    }

    public void startTrack() {
        nativeStart(self);
    }

    public void stopTrack() {
        synchronized (this) {
            mHandlerThread.quitSafely();
            mHandler.removeCallbacksAndMessages(null);
            nativeStop(self);
            self = 0;
        }
    }

    public void detector(byte[] data) {
        mHandler.removeMessages(11);
        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public FaceData getFaceData() {
        return mFaceData;
    }

    private native long nativeCreate(String model, String seeta);

    private native void nativeStart(long self);

    private native void nativeStop(long self);

    private native FaceData nativeDetector(long self, byte[] data, int cameraId, int width, int height);
}
