package com.karashok.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.karashok.opengldemo.utils.OpenGLUtils;

/**
 * @author karashok
 * @since 05-20-2023
 */
public class AbstractFrameFilter extends AbstractFilter {

    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;

    public AbstractFrameFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        super(context, vertexShaderId, fragmentShaderId);

    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        if (mFrameBuffers != null) {
            destroyFrameBuffers();
        }

        // 1、创建 FBO（离屏屏幕）
        mFrameBuffers = new int[1];

        // 1：创建几个 FBO；2：保存 FBO id 数据；3：从这个数组的第几个开始保存
        GLES20.glGenFramebuffers(mFrameBuffers.length,mFrameBuffers,0);

        // 2、创建属于 FBO 纹理
        mFrameBufferTextures = new int[1];

        // 创建纹理
        OpenGLUtils.glGenTextures(mFrameBufferTextures);

        // 让 FBO 与纹理绑定，创建一个 2d 图像
        // 目标：2d 纹理 + 等级 + 格式 + 宽、高 + 格式 + 数据类型 + 像素数据
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,mOutputWidth,mOutputHeight,
                0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,null);

        // 让 FBO 与纹理绑定起来，后续的操作就是在操作 FBO 与这个纹理上了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,mFrameBufferTextures[0],0);

        // 解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    public void destroyFrameBuffers() {

        // 删除fbo的纹理
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1,mFrameBufferTextures,0);
            mFrameBufferTextures = null;
        }

        // 删除fbo
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1,mFrameBuffers,0);
            mFrameBuffers = null;
        }
    }

    @Override
    public void release() {
        super.release();
        destroyFrameBuffers();
    }
}
