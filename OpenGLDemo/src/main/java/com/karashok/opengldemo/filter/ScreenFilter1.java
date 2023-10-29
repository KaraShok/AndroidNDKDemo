package com.karashok.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.karashok.opengldemo.R;
import com.karashok.opengldemo.utils.OpenUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author karashok
 * @since 04-17-2023
 * 负责往屏幕上渲染
 */
public class ScreenFilter1 {

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;
    private int vTexture;
    private int vMatrix;
    private int vCoord;
    private int vPosition;
    private int mProgram;
    private int mWidth;
    private int mHeight;

    public ScreenFilter1(Context context) {

        // 读取内容
        String vertexSource = OpenUtils.readRawTextFile(context, R.raw.camera_vertex);
        String fragSource = OpenUtils.readRawTextFile(context, R.raw.camera_frag);

        // 创建顶点着色器
        int vShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        // 绑定数据到着色器中
        GLES20.glShaderSource(vShaderId,vertexSource);

        // 编译着色器代码
        GLES20.glCompileShader(vShaderId);

        // 主动获取状态，如果只输出日志很难定位问题
        int[] status = new int[1];
        GLES20.glGetShaderiv(vShaderId,GLES20.GL_COMPILE_STATUS,status,0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 顶点着色器配置失败!");
        }

        // 创建片元着色器，操作同上
        int fShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShaderId,fragSource);
        GLES20.glCompileShader(fShaderId);
        GLES20.glGetShaderiv(fShaderId,GLES20.GL_COMPILE_STATUS,status,0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 片元着色器配置失败!");
        }

        // 创建着色器程序（GPU 上的小程序）
        mProgram = GLES20.glCreateProgram();

        // 将着色器塞到程序中
        GLES20.glAttachShader(mProgram,vShaderId);
        GLES20.glAttachShader(mProgram,fShaderId);

        // 链接着色器
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram,GLES20.GL_LINK_STATUS,status,0);

        // 获取程序是否配置成功
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 着色器程序配置失败!");
        }

        // 已经塞到程序中，所以可以删除了
        GLES20.glDeleteShader(vShaderId);
        GLES20.glDeleteShader(fShaderId);

        // 获得着色器程序中的变量索引，通过
        // 顶点
        vPosition = GLES20.glGetAttribLocation(mProgram,"vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgram, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //片元
        vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");

        // 创建一个数据缓冲区；4 个点，每个点两个数据 (x,y)，每个数据 4 个字节，数据类型float
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.clear();

        float[] v = {
                -1.0f,-1.0f,
                1.0f,-1.0f,
                -1.0f,1.0f,
                1.0f,1.0f
        };
        mVertexBuffer.put(v);

        mTextureBuffer = ByteBuffer.allocateDirect( 4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.clear();

//        float[] t = {
//                0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f
//        };

        // 旋转
//        float[] t = {
//                1.0f, 1.0f,
//                1.0f, 0.0f,
//                0.0f, 1.0f,
//                0.0f, 0.0f
//        };

        // 镜像
        float[] t = {
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f
        };
        mTextureBuffer.put(t);
    }

    /**
     * 使用着色器程序进行绘画
     * @param texture
     * @param mtx
     */
    public void onDrawFrame(int texture, float[] mtx) {

        // 设置窗口大小
        // 绘制时，你的画布可以看成 10x10，也可以看成 5x5 等等
        // 设置画布的大小，然后绘制的时候，画布越大，绘制的图像就会显得越小
        GLES20.glViewport(0,0,mWidth,mHeight);

        // 使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 怎么画，其实就是传值
        // 将顶点数据传入，确定形状；xy 两个数据，float 的类型
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,0,mVertexBuffer);

        // 传了数据之后，激活
        GLES20.glEnableVertexAttribArray(vPosition);

        // 将纹理坐标传入，采样坐标
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,mTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        // 变换矩阵
        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);

        // 片元 vTexture 绑定图像数据到采样器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // 图像数据，正常 GLES20.GL_TEXTURE_2D，SurfaceTexture 的纹理需要
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,texture);

        // 传递参数，0 需要和纹理层 GL_TEXTURE0 对应
        GLES20.glUniform1i(vTexture,0);

        // 参数传完，通知 OpenGL 绘制，从第 0 点开始，共 4 个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    public void onReady(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
}
