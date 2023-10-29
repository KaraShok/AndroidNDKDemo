//
// Created by KaraShokZ on 2023/3/4.
//

#ifndef NDKDEMO_AUDIOCHANNEL_H
#define NDKDEMO_AUDIOCHANNEL_H

#include "librtmp/rtmp.h"
#include "include/faac/faac.h"
#include <sys/types.h>
#include "macro.h"
#include <cstring>

class AudioChannel {
    typedef void (*AudioCallBack)(RTMPPacket *packet);

public:

    AudioChannel();

    ~AudioChannel();

    void setAudioEncInfo(int samplesInHZ, int channels);

    void setAudioCallBack(AudioCallBack callBack);

    int getInputSamples();

    void encodeData(int8_t *data);

    RTMPPacket* getAudioTag();

private:

    AudioCallBack audioCallBack;
    int mChannels;
    faacEncHandle  audioCodec = 0;
    u_long inputSamples;
    u_long maxOutputBytes;
    u_char *buffer = 0;
};

#endif //NDKDEMO_AUDIOCHANNEL_H
