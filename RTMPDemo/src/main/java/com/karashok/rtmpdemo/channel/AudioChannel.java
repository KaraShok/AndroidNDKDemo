package com.karashok.rtmpdemo.channel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author karashok
 * @since 02-28-2023
 */
public class AudioChannel {

    private int inputSamples;
    private ExecutorService executor;
    private AudioRecord audioRecord;
    private int channels = 1;
    private boolean isLiving;
    private LivePusher pusher;
    private int sampleRateInHz = 44100;

    public AudioChannel(LivePusher pusher) {
        this.pusher = pusher;
        executor = Executors.newSingleThreadExecutor();

        // 准备录音机 采集pcm 数据
        int channelConfig;
        if (channels == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        } else {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }

        pusher.nativeSetAudioEncInfo(sampleRateInHz,channels);

        // 16位 2个字节
        inputSamples = pusher.nativeGetInputSamples() * 2;

        // 最小需要的缓冲区
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig,AudioFormat.ENCODING_PCM_16BIT) * 2;

        // 1、麦克风 2、采样率 3、声道数 4、采样位
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz,channelConfig,AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize > inputSamples ? minBufferSize : inputSamples);
    }

    public void startLive() {
        isLiving = true;
        executor.submit(new AudioTask());
    }

    public void stopLive() {
        isLiving = false;
        executor.shutdown();
    }

    public void release() {
        audioRecord.release();
    }

    private class AudioTask implements Runnable {

        @Override
        public void run() {
            audioRecord.startRecording();
            byte[] bytes = new byte[inputSamples];
            while (isLiving) {
                int len = audioRecord.read(bytes,0,bytes.length);
                if (len > 0) {
                    pusher.nativePushAudio(bytes);
                }
            }
            audioRecord.stop();
        }
    }
}
