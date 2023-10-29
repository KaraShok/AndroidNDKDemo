package com.karashok.andfixdemo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * @author karashok
 * @since 03-11-2023
 */
public class DexManager {

    private Context context;

    public DexManager(Context context) {
        this.context = context;
    }

    public void load(Class cls) {
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            Replace anno = m.getAnnotation(Replace.class);
            if (anno == null) {
                continue;
            }
            String clazzName = anno.clazz();
            String methodName = anno.method();
            Log.d("DemoDexManager", "load: " + clazzName + " " + methodName);
            try {
                Class<?> wClass = Class.forName(clazzName);
                Method wMethod = wClass.getDeclaredMethod(methodName, m.getParameterTypes());
                replace(wMethod,m);
                Toast.makeText(context,"修复完成",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            }
        }
    }

    public void load(File file) {
        try {
            DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),
                    new File(context.getCacheDir(), "opt").getAbsolutePath(),
                    Context.MODE_PRIVATE);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String clsName = entries.nextElement();
                Class cls = dexFile.loadClass(clsName, context.getClassLoader());
                if (cls != null) {
                    fixClass(cls);
                }
            }
        } catch (Exception e) {

        }
    }

    private void fixClass(Class cls) {
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            Replace anno = m.getAnnotation(Replace.class);
            if (anno == null) {
                continue;
            }
            String clazzName = anno.clazz();
            String methodName = anno.method();
            try {
                Class<?> wClass = Class.forName(clazzName);
                Method wMethod = wClass.getDeclaredMethod(methodName, m.getParameterTypes());
                replace(wMethod,m);
                Toast.makeText(context,"修复完成",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            }
        }
    }

    public native void replace(Method wMethod, Method rMethod);
}
