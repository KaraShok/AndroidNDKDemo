#include <jni.h>
#include <string>
#include "librtmp/rtmp.h"
#include "safe_queue.h"
#include "macro.h"
#include "AudioChannel.h"
#include "VideoChannel.h"
#include "include/x264/x264.h"
#include "include/faac/faac.h"

#ifndef _Included_com_karashok_rtmpdemo_channel_LivePusher
#define _Included_com_karashok_rtmpdemo_channel_LivePusher
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL Java_com_karashok_rtmpdemo_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    faacEncHandle handle;
    std::string hello = "RTMP Demo \n rtmp - " +
                        std::to_string(RTMP_LibVersion()) +
                        "\n x264 - " +
                        std::to_string(X264_BUILD);
    return env->NewStringUTF(hello.c_str());
}

SafeQueue<RTMPPacket *> packets;
VideoChannel *videoChannel = 0;
int isStart = 0;
pthread_t pid;

int readyPushing = 0;
uint32_t start_time;

AudioChannel *audioChannel = 0;

void releasePackets(RTMPPacket *&packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = 0;
    }
}

void callback(RTMPPacket *packet) {
    if (packet) {
        //设置时间戳
        packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        packets.push(packet);
    }
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeInit
        (JNIEnv *env, jobject instance) {
    videoChannel = new VideoChannel;
    videoChannel->setVideoCallBack(callback);
    audioChannel = new AudioChannel;
    audioChannel->setAudioCallBack(callback);
    packets.setReleaseCallback(releasePackets);
}

void *runStart(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do {
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("alloc rtmp失败");
            break;
        }
        RTMP_Init(rtmp);
        int ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("设置地址失败:%s", url);
            break;
        }
        //5s超时时间
        rtmp->Link.timeout = 5;
        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("连接服务器:%s", url);
            break;
        }
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("连接流:%s", url);
            break;
        }
        //记录一个开始时间
        start_time = RTMP_GetTime();
        //表示可以开始推流了
        readyPushing = 1;
        packets.setWork(1);
        //保证第一个数据是 aac解码数据包
        callback(audioChannel->getAudioTag());
        RTMPPacket *packet = 0;
        while (readyPushing) {
            packets.pop(packet);
            if (!readyPushing) {
                break;
            }
            if (!packet) {
                continue;
            }
            packet->m_nInfoField2 = rtmp->m_stream_id;
            //发送rtmp包 1：队列
            // 意外断网？发送失败，rtmpdump 内部会调用RTMP_Close
            // RTMP_Close 又会调用 RTMP_SendPacket
            // RTMP_SendPacket  又会调用 RTMP_Close
            // 将rtmp.c 里面WriteN方法的 Rtmp_Close注释掉
            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePackets(packet);
            if (!ret) {
                LOGE("发送失败");
                break;
            }
        }
        releasePackets(packet);
    } while (0);
    //
    isStart = 0;
    readyPushing = 0;
    packets.setWork(0);
    packets.clear();
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    delete (url);
    return 0;

}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeStart
        (JNIEnv *env, jobject instance, jstring path) {
    if (isStart) {
        return;
    }
    isStart = 1;
    const char *url = env->GetStringUTFChars(path,0);
    char *pu = new char[strlen(url) + 1];
    strcpy(pu,url);
    pthread_create(&pid,0,runStart,pu);
    env->ReleaseStringUTFChars(path,url);
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeSetVideoEncInfo
        (JNIEnv *env, jobject instance, jint width, jint height, jint fps, jint bitrate) {
    if (videoChannel) {}
    videoChannel->setVideoEncInfo(width,height,fps,bitrate);
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeSetAudioEncInfo
        (JNIEnv *env, jobject instance, jint sampleRateInHz, jint channels) {
    if (audioChannel) {
        audioChannel->setAudioEncInfo(sampleRateInHz,channels);
    }
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativePushVideo
        (JNIEnv *env, jobject instance, jbyteArray data) {
    if (!videoChannel || !readyPushing) {
        return;
    }
    jbyte *da = env->GetByteArrayElements(data,NULL);
    videoChannel->encodeData(da);
    env->ReleaseByteArrayElements(data,da,0);
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeStop
        (JNIEnv *env, jobject instance) {
    readyPushing = 0;
    packets.setWork(0);
    pthread_join(pid,0);
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeRelease
        (JNIEnv *, jobject) {
    DELETE(videoChannel);
    DELETE(audioChannel);
}

JNIEXPORT jint JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativeGetInputSamples
        (JNIEnv *env, jobject instance) {
    if (audioChannel) {
        return audioChannel->getInputSamples();
    }
    return -1;
}

JNIEXPORT void JNICALL Java_com_karashok_rtmpdemo_channel_LivePusher_nativePushAudio
        (JNIEnv *env, jobject instance, jbyteArray data) {
    if (!audioChannel || !readyPushing) {
        return;
    }
    jbyte *da = env->GetByteArrayElements(data,NULL);
    audioChannel->encodeData(da);
    env->ReleaseByteArrayElements(data,da,0);
}

#ifdef __cplusplus
}
#endif
#endif