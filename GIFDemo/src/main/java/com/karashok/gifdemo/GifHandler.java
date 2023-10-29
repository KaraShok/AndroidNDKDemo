package com.karashok.gifdemo;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author karashok
 * @since 03-12-2023
 * 仅能解析 98a 的 gif
 */
public class GifHandler {

    private long gifAddr;

    public void loadPath(String path) {
        gifAddr = nativeLoadPath(path);
    }

    public int getWidth() {
        return nativeGetWidth(gifAddr);
    }

    public int getHeight() {
        return nativeGetHeight(gifAddr);
    }

    public int updateFrame(Bitmap bitmap) {
        Log.d("GifHandler", "updateFrame: bitmap is null " + (bitmap == null));
        return nativeUpdateFrame(gifAddr,bitmap);
    }

    public native long nativeLoadPath(String path);

    public native int nativeGetWidth(long ga);

    public native int nativeGetHeight(long ga);

    public native int nativeUpdateFrame(long ga, Bitmap bitmap);
}
