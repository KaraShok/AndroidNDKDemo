package com.karashok.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.karashok.opengldemo.R;
import com.karashok.opengldemo.utils.face.FaceData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author karashok
 * @since 05-20-2023
 */
public class BigEyeFilter extends AbstractFrameFilter{

    private int leftEye;
    private int rightEye;
    private FloatBuffer left;
    private FloatBuffer right;
    private FaceData mFaceData;

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.bigeye_frag);

        leftEye = GLES20.glGetUniformLocation(mGLProgramId,"leftEye");
        rightEye = GLES20.glGetUniformLocation(mGLProgramId,"rightEye");

        left = ByteBuffer.allocateDirect(2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        right = ByteBuffer.allocateDirect(2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    public void setFaceData(FaceData faceData) {
        mFaceData = faceData;
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();

        // 从opengl画到opengl 不是画到屏幕， 修改坐标
        float[] TEXTURE = {
                0.0f,0.0f,
                1.0f,0.0f,
                0.0f,1.0f,
                1.0f,1.0f
        };
        mGLTextureBuffer.put(TEXTURE);
    }

    @Override
    public int onDrawFrame(int textureId) {
        if (mFaceData == null) {
            return textureId;
        }

        // 设置显示窗口
        GLES20.glViewport(0,0,mOutputWidth,mOutputHeight);

        // 不调用的话就是默认的操作 glSurfaceView 中的纹理了，就会显示到屏幕上
        // 这里我们还只是把它画到 FBO 中（缓存）
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mFrameBuffers[0]);

        // 使用着色器
        GLES20.glUseProgram(mGLProgramId);

        // 传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,0,mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        // 传递眼睛的坐标 给 GLSL
        float[] landmarks = mFaceData.landMarks;

        // 左眼
        float x = landmarks[2] / mFaceData.imgWidth;
        float y = landmarks[3] / mFaceData.imgHeight;
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(leftEye,1,left);

        // 右眼
        x = landmarks[4] / mFaceData.imgWidth;
        y = landmarks[5] / mFaceData.imgHeight;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(rightEye,1,right);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glUniform1i(vTexture,0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        // 返回 FBO 的纹理 id
        return mFrameBufferTextures[0];
    }
}
