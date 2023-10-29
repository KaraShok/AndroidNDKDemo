package com.karashok.opengldemo.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.karashok.opengldemo.filter.ScreenFilter;

/**
 * @author karashok
 * @since 05-10-2023
 * EGL配置与录制的opengl操作工具类
 */
public class EGLBase {

    private ScreenFilter mScreenFilter;
    private EGLSurface mEglSurface;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;

    public EGLBase(Context context, int width, int height, Surface surface, EGLContext eglContext) {

        // 创建 EGL 环境
        createEGL(eglContext);

        int[] attribList = {EGL14.EGL_NONE};

        // 把 Surface 贴到 mEglDisplay 中。绘制线程中的图像，就是往 mEglSurface 上面绘制
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay,mEglConfig,surface,attribList,0);

        // 绑定当前现场的显示设备上下文
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw new RuntimeException("eglMakeCurrent 失败！");
        }

        // 虚拟屏幕
        mScreenFilter = new ScreenFilter(context);
        mScreenFilter.onReady(width, height);
    }

    private void createEGL(EGLContext eglContext) {

        // 创建虚拟显示器
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        // 初始化显示器
        int[] version = new int[2];
        // [0] 主版本号，[1] 子版本号
        if (!EGL14.eglInitialize(mEglDisplay,version,0,version,1)) {
            throw new RuntimeException("eglInitialize failed");
        }

        // 根据我们配置的属性，选择配置
        int[] attribList = {
                EGL14.EGL_RED_SIZE,8, // 缓冲区中 红分量位数
                EGL14.EGL_GREEN_SIZE,8,
                EGL14.EGL_BLUE_SIZE,8,
                EGL14.EGL_ALPHA_SIZE,8,
                EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT, // EGL 版本 2.x
                EGL14.EGL_NONE // 结尾标志
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];

        // attribList：属性列表；configs：获取的配置；numConfig 长度和 configs 保持一致即可
        if (!EGL14.eglChooseConfig(mEglDisplay,attribList,0,configs,0,configs.length,numConfig,0)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        mEglConfig = configs[0];
        int[] ctxAttribList = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,2, //  EGL 版本 2.x
                EGL14.EGL_NONE
        };

        // eglContext：共享上下文，传绘制线程中的 EGL 上下文，达到共享资源的目的
        mEglContext = EGL14.eglCreateContext(mEglDisplay,mEglConfig,eglContext,ctxAttribList,0);

        // 创建失败
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL Context Error.");
        }
    }

    public void draw(int textureId, long timestamp) {
        // 绑定当前线程的显示设备及上下文，之后操作 OpenGL 就是在这个虚拟显示上操作
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw  new RuntimeException("eglMakeCurrent 失败！");
        }

        // 绘制到虚拟屏幕上
        mScreenFilter.onDrawFrame(textureId);

        // 刷新 EGLSurface 的时间戳
        EGLExt.eglPresentationTimeANDROID(mEglDisplay,mEglSurface,timestamp);

        // 交换数据，EGL 工作模式是双缓存模式
        EGL14.eglSwapBuffers(mEglDisplay,mEglSurface);
    }

    /**
     * 回收
     */
    public void release() {
        EGL14.eglDestroySurface(mEglDisplay,mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay,mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }

}
