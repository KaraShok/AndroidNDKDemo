package com.karashok.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.karashok.opengldemo.R;

/**
 * @author karashok
 * @since 05-25-2023
 */
public class BeautyFilter extends AbstractFrameFilter {

    private int width;
    private int height;

    public BeautyFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.beauty_frag1);
        width = GLES20.glGetUniformLocation(mGLProgramId,"width");
        height = GLES20.glGetUniformLocation(mGLProgramId,"height");
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
        GLES20.glViewport(0,0,mOutputWidth,mOutputHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mFrameBuffers[0]);

        GLES20.glUseProgram(mGLProgramId);
        GLES20.glUniform1i(width,mOutputWidth);
        GLES20.glUniform1i(height,mOutputHeight);

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

        return mFrameBufferTextures[0];
    }
}
