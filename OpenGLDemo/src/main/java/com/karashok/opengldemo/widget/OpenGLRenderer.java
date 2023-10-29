package com.karashok.opengldemo.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.karashok.opengldemo.filter.BeautyFilter;
import com.karashok.opengldemo.filter.BigEyeFilter;
import com.karashok.opengldemo.filter.CameraFilter;
import com.karashok.opengldemo.filter.ScreenFilter;
import com.karashok.opengldemo.filter.StickerFilter;
import com.karashok.opengldemo.record.VideoRecorder;
import com.karashok.opengldemo.utils.CameraHelper;
import com.karashok.opengldemo.utils.OpenGLUtils;
import com.karashok.opengldemo.utils.OpenUtils;
import com.karashok.opengldemo.utils.face.FaceTrack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author karashok
 * @since 04-17-2023
 */
public class OpenGLRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {

    private ScreenFilter screenFilter;
    private CameraFilter cameraFilter;
    private BigEyeFilter bigEyeFilter;
    private StickerFilter stickerFilter;
    private BeautyFilter beautyFilter;

    private FaceTrack faceTrack;

    private OpenGLView mView;
    private CameraHelper cameraHelper;
    private SurfaceTexture surfaceTexture;
    private float[] mtx = new float[16];
    private int[] mTextures;
    private VideoRecorder mVideoRecorder;
    private VideoRecorder.OnRecordFinishListener finishListener;
    private String frontalface;
    private String seeta;

    public OpenGLRenderer(OpenGLView view) {
        mView = view;

        frontalface = OpenUtils.copyAssetsToSdcard(view.getContext(), "lbpcascade_frontalface.xml");
        seeta = OpenUtils.copyAssetsToSdcard(view.getContext(), "seeta_fa_v1.1.bin");
    }

    /**
     * 画布已经创建
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 初始化
        cameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK);

        // 准备好摄像头绘制的画布
        // 通过 OpenGL 创建一个纹理 Id
        mTextures = new int[1];
        GLES20.glGenTextures(mTextures.length,mTextures,0);
        surfaceTexture = new SurfaceTexture(mTextures[0]);
        surfaceTexture.setOnFrameAvailableListener(this);

        // 必须在 OpenGL 线程操作 OpenGL
        cameraFilter = new CameraFilter(mView.getContext());
        screenFilter = new ScreenFilter(mView.getContext());
        bigEyeFilter = new BigEyeFilter(mView.getContext());
        stickerFilter = new StickerFilter(mView.getContext());
        beautyFilter = new BeautyFilter(mView.getContext());

        mVideoRecorder = new VideoRecorder(mView.getContext(), "/sdcard/a.mp4",
                CameraHelper.HEIGHT,CameraHelper.WIDTH, EGL14.eglGetCurrentContext());
        mVideoRecorder.setOnRecordFinishListener(finishListener);
    }

    /**
     * 画布状态发生改变
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        faceTrack = new FaceTrack(frontalface,seeta, cameraHelper);
        faceTrack.startTrack();

        // 开启预览
        cameraHelper.startPreview(surfaceTexture);
        cameraHelper.setPreviewCallback(this);
        cameraFilter.onReady(width, height);
        bigEyeFilter.onReady(width, height);
        stickerFilter.onReady(width, height);
        beautyFilter.onReady(width, height);

        screenFilter.onReady(width, height);
    }

    public void onSurfaceDestroyed() {
        cameraHelper.stopPreview();
        faceTrack.stopTrack();
    }

    /**
     * 开始绘制
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {

        // 配置清理屏幕颜色，把屏幕清理成需要的颜色
        GLES20.glClearColor(0,0,0,0);

        // 执行屏幕清理颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 把屏幕数据先输出；更新纹理，然后才能够使用 OpenGL 获取数据进行渲染
        surfaceTexture.updateTexImage();

        // SurfaceTexture 比较特殊，使用的是 samplerExternalOES 特殊的采样器
        // 变换矩阵
        surfaceTexture.getTransformMatrix(mtx);
        cameraFilter.setMatrix(mtx);

        int textureId = cameraFilter.onDrawFrame(mTextures[0]);

        bigEyeFilter.setFaceData(faceTrack.getFaceData());
        textureId = bigEyeFilter.onDrawFrame(textureId);

        stickerFilter.setFaceData(faceTrack.getFaceData());
        textureId = stickerFilter.onDrawFrame(textureId);

        textureId = beautyFilter.onDrawFrame(textureId);

        screenFilter.onDrawFrame(textureId);

        mVideoRecorder.encodeFrame(textureId,surfaceTexture.getTimestamp());
    }

    /**
     * 有一个有效的新数据的时候回调
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mView.requestRender();
    }

    public void startRecord(float speed) {
        mVideoRecorder.start(speed);
    }

    public void stopRecord() {
        mVideoRecorder.stop();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        faceTrack.detector(data);
    }

    public void switchCamera() {
        cameraHelper.switchCamera();
    }

    public void setOnRecordFinishListener(VideoRecorder.OnRecordFinishListener listener){
        finishListener = listener;
    }
}
