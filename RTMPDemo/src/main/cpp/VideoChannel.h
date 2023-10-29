//
// Created by KaraShokZ on 2023/3/4.
//

#ifndef NDKDEMO_VIDEOCHANNEL_H
#define NDKDEMO_VIDEOCHANNEL_H

#include "inttypes.h"
#include "include/x264/x264.h"
#include "pthread.h"
#include "librtmp/rtmp.h"
#include "macro.h"
#include <cstring>

class VideoChannel {
    typedef void (*VideoCallBack)(RTMPPacket *packet);

public:

    VideoChannel();

    ~VideoChannel();

    void setVideoEncInfo(int width, int height, int fps, int bitrate);

    void encodeData(int8_t *data);

    void setVideoCallBack(VideoCallBack back);

private:

    pthread_mutex_t mutex;
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;
    x264_t *videoCodec = 0;
    x264_picture_t *pic_in = 0;

    int ySize;
    int uvSize;
    VideoCallBack videoCallBack;

    void sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len);

    void sendFrame(int type, uint8_t *payload, int i_payload);
};
#endif //NDKDEMO_VIDEOCHANNEL_H
