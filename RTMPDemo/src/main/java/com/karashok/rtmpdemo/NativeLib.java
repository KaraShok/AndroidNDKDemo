package com.karashok.rtmpdemo;

public class NativeLib {

    // Used to load the 'rtmpdemo' library on application startup.
    static {
        System.loadLibrary("rtmpdemo");
    }

    /**
     * A native method that is implemented by the 'rtmpdemo' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}