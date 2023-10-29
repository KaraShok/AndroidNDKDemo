package com.karashok.opengldemo.soul;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * @author karashok
 * @since 05-29-2023
 */
public class VideoDecorder {

    private ISurface mISurface;
    private String mPath;
    private MediaExtractor mMediaExtractor;
    private int mWidth;
    private int mHeight;
    private int mFps;
    private MediaCodec mMediaCodec;
    private boolean isCodeing;
    private byte[] outData;
    private CodecTask mCodecTask;

    public void setDisplay(ISurface iSurface) {
        mISurface = iSurface;
    }

    public void setDataSource(String path) {
        mPath = path;
    }

    public void prepare() {
        mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(mPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int videoIndex = -1;
        MediaFormat videoMediaFormat = null;
        int trackCount = mMediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
            String mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video/")) {
                videoIndex = i;
                videoMediaFormat = trackFormat;
                break;
            }
        }

        if (videoMediaFormat != null) {
            mWidth = videoMediaFormat.getInteger(MediaFormat.KEY_WIDTH,0);
            mHeight = videoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT,0);
            mFps = videoMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE,20);
            videoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);

            try {
                mMediaCodec = MediaCodec.createDecoderByType(videoMediaFormat.getString(MediaFormat.KEY_MIME));
                mMediaCodec.configure(videoMediaFormat,null,null,0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMediaExtractor.selectTrack(videoIndex);
        }

        if (mISurface != null) {
            mISurface.setVideoParams(mWidth,mHeight,mFps);
        }
    }

    public void start() {
        isCodeing = true;
        outData = new byte[mWidth * mHeight * 3 / 2];
        mCodecTask = new CodecTask();
        mCodecTask.start();
    }

    public void stop() {
        isCodeing = false;
        if (mCodecTask != null && mCodecTask.isAlive()) {
            try {
                mCodecTask.join(3_000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mCodecTask.isAlive()) {
                mCodecTask.interrupt();
            }
            mCodecTask = null;
        }
    }

    private class CodecTask extends Thread {

        @Override
        public void run() {
            if (mMediaCodec == null) {
                return;
            }

            mMediaCodec.start();
            boolean isEOF = false;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (!isInterrupted()) {
                if (!isCodeing) {
                    break;
                }

                if (!isEOF) {
                    isEOF = putBuffer2Codec();
                }

                int status = mMediaCodec.dequeueOutputBuffer(bufferInfo,100);
                if (status >= 0) {
                    ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(status);
                    if (bufferInfo.size == outData.length) {
                        outputBuffer.get(outData);
                        if (mISurface != null) {
                            mISurface.offer(outData);
                        }
                    }
                    mMediaCodec.releaseOutputBuffer(status,false);
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            mMediaExtractor.release();
            mMediaExtractor = null;
        }

        private boolean putBuffer2Codec() {
            int status = mMediaCodec.dequeueInputBuffer(100);
            if (status >= 0) {
                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(status);
                inputBuffer.clear();
                int size = mMediaExtractor.readSampleData(inputBuffer,0);
                if (size < 0) {
                    mMediaCodec.queueInputBuffer(status,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    return true;
                } else {
                    mMediaCodec.queueInputBuffer(status,0,size,mMediaExtractor.getSampleTime(),0);
                    mMediaExtractor.advance();
                }
                return false;
            }
            return false;
        }
    }
}
