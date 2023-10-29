package com.karashok.opengldemo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.karashok.opengldemo.R;
import com.karashok.opengldemo.utils.OpenGLUtils;
import com.karashok.opengldemo.utils.face.FaceData;

/**
 * @author karashok
 * @since 05-24-2023
 */
public class StickerFilter extends AbstractFrameFilter {

    private Bitmap mBitmap;
    private int[] mTextureId;
    private FaceData mFaceData;

    public StickerFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
        mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.erduo_000);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        mTextureId = new int[1];
        OpenGLUtils.glGenTextures(mTextureId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,mBitmap,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    public void setFaceData(FaceData faceData) {
        this.mFaceData = faceData;
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


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glUniform1i(vTexture,0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        onDrawSticker();

        // 返回 FBO 的纹理 id
        return mFrameBufferTextures[0];
    }

    private void onDrawSticker() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float x = mFaceData.landMarks[0];
        float y = mFaceData.landMarks[1];
        x = x / mFaceData.imgWidth * mOutputWidth;
        y = y / mFaceData.imgHeight * mOutputHeight;

        GLES20.glViewport((int) x, (int) y - mBitmap.getHeight() / 2,
                (int) (1f * mFaceData.width / mFaceData.imgWidth * mOutputWidth),
                mBitmap.getHeight());

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mFrameBuffers[0]);
        GLES20.glUseProgram(mGLProgramId);

        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,0,mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId[0]);
        GLES20.glUniform1i(vTexture,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }
}
