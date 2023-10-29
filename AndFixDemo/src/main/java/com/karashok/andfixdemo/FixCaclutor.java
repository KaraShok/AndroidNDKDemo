package com.karashok.andfixdemo;

import android.content.Context;
import android.widget.Toast;

/**
 * @author karashok
 * @since 03-11-2023
 */
public class FixCaclutor {

    @Replace(clazz = "com.karashok.andfixdemo.Caclutor", method = "test")
    public void fix(Context context) {
        Toast.makeText(context,"修复好了",Toast.LENGTH_LONG).show();
    }
}
