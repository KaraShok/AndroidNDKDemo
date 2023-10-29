package com.karashok.opengldemo.soul;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author karashok
 * @since 05-29-2023
 */
public class SoulView extends GLSurfaceView implements GLSurfaceView.Renderer, ISurface {

    private int mWidth;
    private int mHeight;
    private int mFps;
    private LinkedList<byte[]> queue;
    private long lastRenderTime;
    private int interval;
    private VideoDecorder videoDecorder;
    private SoulFilter soulFilter;

    public SoulView(Context context) {
        this(context,null);
    }

    public SoulView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        queue = new LinkedList<>();
        videoDecorder = new VideoDecorder();
        videoDecorder.setDisplay(this);
    }

    public void setDataSource(String path) {
        videoDecorder.setDataSource(path);
    }

    public void startPlay() {
        videoDecorder.prepare();
        videoDecorder.start();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        soulFilter = new SoulFilter(getContext());
        soulFilter.onReady(mWidth,mHeight,mFps);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        videoDecorder.stop();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long diff = System.nanoTime() - lastRenderTime;
        long delay = interval - diff;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        byte[] yuv = poll();
        if (yuv != null) {
            soulFilter.onDrawFrame(yuv);
        }
        lastRenderTime = System.nanoTime();
    }

    @Override
    public void offer(byte[] data) {
        synchronized (this) {
            byte[] yuv = new byte[data.length];
            System.arraycopy(data,0,yuv,0,data.length);
            queue.offer(yuv);
        }
    }

    @Override
    public byte[] poll() {
        synchronized (this) {
            return queue.poll();
        }
    }

    @Override
    public void setVideoParams(int width, int height, int fps) {
        mWidth = width;
        mHeight = height;
        mFps = fps;
        interval = 1000 / fps;
    }
}
