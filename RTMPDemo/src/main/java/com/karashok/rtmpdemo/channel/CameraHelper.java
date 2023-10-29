package com.karashok.rtmpdemo.channel;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author karashok
 * @since 02-28-2023
 */
public class CameraHelper implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Activity activity;
    private int height;
    private int width;
    private int cameraId;
    private Camera camera;
    private byte[] buffer;
    private SurfaceHolder holder;
    private Camera.PreviewCallback previewCallback;
    private int rotation;
    private OnChangedSizeListener changedSizeListener;
    private byte[] bytes;

    public CameraHelper(Activity activity, int height, int width, int cameraId) {
        this.activity = activity;
        this.height = height;
        this.width = width;
        this.cameraId = cameraId;
    }

    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();
    }

    private void startPreview() {
        try {
            camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureFormat(ImageFormat.NV21);
            setPreviewSize(parameters);
            setPreviewOrientation(parameters);
            camera.setParameters(parameters);
            buffer = new byte[width * height *3 / 2];
            bytes = new byte[buffer.length];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    private void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = previewSizes.get(0);
        int m = Math.abs(size.height * size.width - width * height);
        previewSizes.remove(0);
        for (Camera.Size s : previewSizes) {
            int n = Math.abs(s.height * s.width - width * height);
            if (n < m) {
                m = n;
                size = s;
            }
        }
        width = size.width;
        height = size.height;
        parameters.setPreviewSize(width,height);
    }

    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,info);
        rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                changedSizeListener.onChanged(height,width);
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                degrees = 90;
                changedSizeListener.onChanged(width,height);
                break;
            case Surface.ROTATION_270: //  横屏 头部在右边
                degrees = 270;
                changedSizeListener.onChanged(width,height);
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            result = (info.orientation + degrees) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void setPreviewDisplay(SurfaceHolder sh) {
        holder = sh;
        holder.addCallback(this);
    }

    public void setPreviewCallback(Camera.PreviewCallback pc) {
        previewCallback = pc;
    }

    public void setOnChangedSizeListener(OnChangedSizeListener listener) {
        changedSizeListener = listener;
    }

    public void release() {
        holder.removeCallback(this);
        stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        switch (rotation) {
            case Surface.ROTATION_0:
                rotation90(data);
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                break;
        }

        // data数据依然是倒的
        previewCallback.onPreviewFrame(bytes, camera);
        camera.addCallbackBuffer(buffer);
    }

    private void rotation90(byte[] data) {
        int index = 0;
        int ySize = width * height;
        //u和v
        int uvHeight = height / 2;
        //后置摄像头顺时针旋转90度
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //将y的数据旋转之后 放入新的byte数组
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    bytes[index++] = data[width * j + i];
                }
            }

            //每次处理两个数据
            for (int i = 0; i < width; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    bytes[index++] = data[ySize + width * j + i];
                    // u
                    bytes[index++] = data[ySize + width * j + i + 1];
                }
            }
        } else {
            //逆时针旋转90度
            for (int i = 0; i < width; i++) {
                int nPos = width - 1;
                for (int j = 0; j < height; j++) {
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
            //u v
            for (int i = 0; i < width; i += 2) {
                int nPos = ySize + width - 1;
                for (int j = 0; j < uvHeight; j++) {
                    bytes[index++] = data[nPos - i - 1];
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopPreview();
    }

    public interface OnChangedSizeListener {
        void onChanged(int w, int h);
    }
}
