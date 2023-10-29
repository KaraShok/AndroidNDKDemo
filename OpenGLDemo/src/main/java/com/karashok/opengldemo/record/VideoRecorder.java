package com.karashok.opengldemo.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author karashok
 * @since 05-10-2023
 */
public class VideoRecorder {

    private Context mContext;
    private String mPath;
    private int mWidth;
    private int mHeight;
    private EGLContext mEglContext;
    private MediaCodec mMediaCodec;
    private Surface mInputSurface;
    private MediaMuxer mMediaMuxer;
    private Handler mHandler;
    private EGLBase mEglBase;
    private boolean isStart;
    private int mIndex;
    private float mSpeed;

    public VideoRecorder(Context context,String path,int width,int height,EGLContext eglContext) {
        mContext = context.getApplicationContext();
        mPath = path;
        mWidth = width;
        mHeight = height;
        mEglContext = eglContext;
    }

    public void start(float speed) {
        try {
            mSpeed = speed;

            // 视频格式，avc：高级编码（H.264）
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,mWidth,mHeight);

            // 码率：1500kbs
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,1500_000);

            // 帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,20);

            // 关键帧间隔
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,20);

            // 颜色格式，从 Surface 中获取
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            // 创建编码器
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            // 将参数配置给编码器
            mMediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 创建图像的 Surface
            mInputSurface = mMediaCodec.createInputSurface();

            // 封装器
            mMediaMuxer = new MediaMuxer(mPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            HandlerThread handlerThread = new HandlerThread("VideoRecorder");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 创建 EGL 环境
                    mEglBase = new EGLBase(mContext,mWidth,mHeight,mInputSurface,mEglContext);

                    // 启动编码器
                    mMediaCodec.start();
                    isStart = true;
                }
            });
        } catch (IOException e) {

        }

    }

    public void encodeFrame(final int textureId, final long timestamp) {
        if (!isStart) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                //  把图像绘制到虚拟屏幕
                mEglBase.draw(textureId,timestamp);

                // 从编码器的输出缓冲区获取编码后的数据
                getCodec(false);
            }
        });
    }

    private void getCodec(boolean endOfStream) {

        // 不录制了，给 MediaCodec 一个标记
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream();
        }

        // 输出缓冲区
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        // 将已经编码完的数据都获取到，写出到 mp4 文件
        while (true) {

            // 等等 10ms
            int status = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);

            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // 之后重试，1、需要更多数据；2、可能还没编码完
                // 如果是停止 我继续循环
                // 继续循环 就表示不会接收到新的等待编码的图像
                // 相当于保证mediacodec中所有的待编码的数据都编码完成了，不断地重试 取出编码器中的编码好的数据
                // 标记不是停止 ，我们退出 ，下一轮接收到更多数据再来取输出编码后的数据
                if (!endOfStream) {
                    break;
                }
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 开始编码，就会调用一次
                MediaFormat outputFormat = mMediaCodec.getOutputFormat();

                // 配置封装器，增加一路指定格式的媒体流
                mIndex = mMediaMuxer.addTrack(outputFormat);
                mMediaMuxer.start();
            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // 忽略
            } else {
                // 成功，取出一个有效的输出
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(status);

                // 如果获取的 ByteBuffer 是配置信息，不需要写出到 mp4 文件
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }


                if (bufferInfo.size != 0) {
                    bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);

                    // 根据偏移定位，写出到 mp4 文件
                    outputBuffer.position(bufferInfo.offset);

                    // 可读写的总体长度
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    // 写出
                    mMediaMuxer.writeSampleData(mIndex,outputBuffer,bufferInfo);
                }

                // 回收缓冲区，让 MediaCodec 可以继续使用
                mMediaCodec.releaseOutputBuffer(status,false);

                // 结束
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

    public void stop() {
        isStart = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getCodec(true);
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;

                mMediaMuxer.stop();
                mMediaMuxer.release();
                mMediaMuxer = null;

                mEglBase.release();
                mEglBase = null;
                mInputSurface = null;

                mHandler.getLooper().quitSafely();
                mHandler = null;

                if (mListener != null) {
                    mListener.onRecordFinish(mPath);
                }
            }
        });
    }

    private OnRecordFinishListener mListener;

    public void setOnRecordFinishListener(OnRecordFinishListener listener){
        mListener = listener;
    }

    public interface OnRecordFinishListener{
        void onRecordFinish(String path);
    }
}
