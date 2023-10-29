package com.karashok.opengldemo.soul;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.karashok.opengldemo.R;
import com.karashok.opengldemo.filter.AbstractFilter;
import com.karashok.opengldemo.utils.OpenGLUtils;

/**
 * @author karashok
 * @since 05-30-2023
 * 灵魂出窍
 */
public class SoulFilter extends AbstractFilter {

    private int[] mTextures;

    // 肉体
    private GLImageData bodyImage;

    // 灵魂
    private GLImageData soulImage;
    private int mAlpha;
    private int mSamplerY;
    private int mSamplerU;
    private int mSamplerV;
    private int mFps;
    private float[] matrix = new float[16];
    private int interval;

    public SoulFilter(Context context) {
        super(context, R.raw.soul_vertex, R.raw.soul_frag);
        bodyImage = new GLImageData();
        soulImage = new GLImageData();

        mSamplerY = GLES20.glGetUniformLocation(mGLProgramId,"sampler_y");
        mSamplerU = GLES20.glGetUniformLocation(mGLProgramId,"sampler_u");
        mSamplerV = GLES20.glGetUniformLocation(mGLProgramId,"sampler_v");
        mAlpha = GLES20.glGetUniformLocation(mGLProgramId,"alpha");

        // 3 个纹理 y、u、v
        mTextures = new int[3];
        OpenGLUtils.glGenTextures(mTextures);
    }

    public void onReady(int width,int height,int fps) {
        super.onReady(width, height);
        mFps = fps;
        bodyImage.initSize(width, height);
        soulImage.initSize(width, height);
    }

    public void onDrawFrame(byte[] yuv) {

        // 把 yuv 分离出来，保存在 image 中的 y、u、v 三个变量
        bodyImage.initData(yuv);

        // 分离出的数据有效
        if (!bodyImage.hasImage()) {
            return;
        }

        // 启用着色器程序
        GLES20.glUseProgram(mGLProgramId);

        // 初始化矩阵，不进行任何缩放平移
        Matrix.setIdentityM(matrix,0);

        // 给肉体的，无矩阵变换
        GLES20.glUniformMatrix4fv(vMatrix,1,false,matrix,0);

        // 透明度，肉体不透明
        GLES20.glUniform1f(mAlpha,1);

        // 绘制
        onDrawGLImageData(bodyImage);

        // 混合灵魂
        onDrawSoul(yuv);
    }

    private void onDrawGLImageData(GLImageData image) {

        // 传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,0,mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        // 传递 yuv 数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextures[0]);

        // 把 y 数据与 0 纹理进行绑定
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,mOutputWidth,mOutputHeight,
                0,GLES20.GL_LUMINANCE,GLES20.GL_UNSIGNED_BYTE, image.getYBB());
        GLES20.glUniform1i(mSamplerY,0);

        // u 数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextures[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,mOutputWidth / 2,mOutputHeight / 2,
                0,GLES20.GL_LUMINANCE,GLES20.GL_UNSIGNED_BYTE, image.getUBB());
        GLES20.glUniform1i(mSamplerU,1);

        // v 数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextures[2]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,mOutputWidth / 2,mOutputHeight / 2,
                0,GLES20.GL_LUMINANCE,GLES20.GL_UNSIGNED_BYTE, image.getVBB());
        GLES20.glUniform1i(mSamplerV,2);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    private void onDrawSoul(byte[] yuv) {
        interval++;

        // 没保存一个灵魂或者使用次数已经达到上限
        // 灵魂只能使用 x 次，使用完了之后就要更新灵魂
        if (!soulImage.hasImage() || interval > mFps) {

            // 重置为 1
            interval = 1;

            // 记录新灵魂
            soulImage.initData(yuv);
        }

        if (!soulImage.hasImage()) {
            return;
        }

        GLES20.glEnable(GLES20.GL_BLEND);

        // 1：源 灵魂  GL_ONE:画灵魂自己
        // 2: 肉体  也是肉体自己
        // 两个都是用自己原本的颜色去混合
        // GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE);
        // 让灵魂整体颜色变淡
        // GL_SRC_ALPHA： 取源(灵魂)的alpha 作为因子
        // 假设alpha是0.2 rgb都是1 -> 混合就是用 rgb都是 0.2*1 整体变淡
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE);

        Matrix.setIdentityM(matrix,0);

        // 设置缩放大小 本次放大为 1+当前灵魂次数占总次数*2的比例
        // 不一次放太大 为了达到较好的表现效果 fps*2
        // 所以这里值为 1+1/60 ---> 1+20/40 1.025... ---> 1.5
        float scale = 1.0f + interval / (mFps * 2.0f);
        Matrix.scaleM(matrix,0,scale,scale,0);

        GLES20.glUniformMatrix4fv(vMatrix,1,false,matrix,0);
        GLES20.glUniform1f(mAlpha,0.1f + (mFps - interval) / 100.0f);
        onDrawGLImageData(soulImage);
    }
}
