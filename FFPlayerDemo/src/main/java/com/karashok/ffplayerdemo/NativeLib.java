package com.karashok.ffplayerdemo;

public class NativeLib {

    // Used to load the 'ffplayerdemo' library on application startup.
    static {
        System.loadLibrary("ffplayerdemo");
    }

    /**
     * A native method that is implemented by the 'ffplayerdemo' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}