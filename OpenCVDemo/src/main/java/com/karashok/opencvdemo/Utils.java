package com.karashok.opencvdemo;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author karashok
 * @since 03-23-2023
 */
public class Utils {

    public static String copyAssetsToSdcard(Context context, String path) {
        File model = new File(path);
        File file = new File(Environment.getExternalStorageDirectory(), model.getName());
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = context.getAssets().open(path);
            int len;
            byte[] b = new byte[2048];
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}
