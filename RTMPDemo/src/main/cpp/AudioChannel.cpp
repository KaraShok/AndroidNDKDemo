//
// Created by KaraShokZ on 2023/3/4.
//

#include "AudioChannel.h"

AudioChannel::AudioChannel() {

}

AudioChannel::~AudioChannel() {
    DELETE(buffer);
    if (audioCodec) {}
    faacEncClose(audioCodec);
    audioCodec = 0;
}

void AudioChannel::setAudioCallBack(AudioCallBack back) {
    this->audioCallBack = back;
}

void AudioChannel::setAudioEncInfo(int samplesInHZ, int channels) {
    // 打开编码器
    mChannels = channels;

    // 3、一次最大能输入编码器的样本数量 也编码的数据的个数 (一个样本是 16 位 2 字节)
    // 4、最大可能的输出数据  编码后的最大字节数
    audioCodec = faacEncOpen(samplesInHZ,channels,&inputSamples,&maxOutputBytes);

    // 设置编码器参数
    faacEncConfigurationPtr config = faacEncGetCurrentConfiguration(audioCodec);

    // 指定为 mpeg4 标准
    config->mpegVersion = MPEG4;

    // lc 标准
    config->aacObjectType = LOW;

    // 16位
    config->inputFormat = FAAC_INPUT_16BIT;

    // 编码出原始数据 既不是adts也不是adif
    config->outputFormat = 0;
    faacEncSetConfiguration(audioCodec,config);

    // 输出缓冲区 编码后的数据 用这个缓冲区来保存
    buffer = new u_char[maxOutputBytes];
}

int AudioChannel::getInputSamples() {
    return inputSamples;
}

RTMPPacket *AudioChannel::getAudioTag() {
    u_char *buf;
    u_long len;
    faacEncGetDecoderSpecificInfo(audioCodec,&buf,&len);
    int bodySize = len + 2;
    RTMPPacket *packet = new RTMPPacket;
    RTMPPacket_Alloc(packet,bodySize);


    if (mChannels == 1) {
        packet->m_body[0] = 0xAE;
    } else {
        // 双声道
        packet->m_body[0] = 0xAF;
    }
    packet->m_body[1] = 0x00;

    // 音频数据
    memcpy(&packet->m_body[2],buf,len);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nBodySize = bodySize;
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nChannel = 0x11;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    return packet;
}

void AudioChannel::encodeData(int8_t *data) {
    // 返回编码后数据字节的长度
    int byteLen = faacEncEncode(audioCodec,
                                reinterpret_cast<int32_t *>(data),
                                inputSamples,
                                buffer,
                                maxOutputBytes);

    if (byteLen > 0) {
        // 看表
        int bodySize = byteLen + 2;
        RTMPPacket *packet = new RTMPPacket;
        RTMPPacket_Alloc(packet,bodySize);

        if (mChannels == 1) {
            packet->m_body[0] = 0xAE;
        } else {
            // 双声道
            packet->m_body[0] = 0xAF;
        }

        // 编码出的声音 都是 0x01
        packet->m_body[1] = 0x01;

        memcpy(&packet->m_body[2],buffer,byteLen);

        packet->m_hasAbsTimestamp = 0;
        packet->m_nBodySize = bodySize;
        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nChannel = 0x11;
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        audioCallBack(packet);
    }
}