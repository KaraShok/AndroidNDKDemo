#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#include <malloc.h>
#include "gif_lib.h"
#define LOG_TAG "gifdemonative"
#define argb(a,r,g,b) ( ((a) & 0xff) << 24 ) | ( ((b) & 0xff) << 16 ) | ( ((g) & 0xff) << 8 ) | ((r) & 0xff)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
typedef struct GifBean{
    int current_frame;
    int total_frame;
    int *dealys;
}GifBean;

void drawFrame(GifFileType *pType, GifBean *pBean, AndroidBitmapInfo info, void *pVoid);

extern "C" JNIEXPORT jlong JNICALL
Java_com_karashok_gifdemo_GifHandler_nativeLoadPath(
        JNIEnv* env,
        jobject instance,
        jstring pathStr) {
    const char *path = env->GetStringUTFChars(pathStr, 0);

    int err;
    GifFileType *gifFileType = DGifOpenFileName(path,&err);
    DGifSlurp(gifFileType);

    LOGE("nativeLoadPath %s err %d",path,err);
    // gif 帧数、总帧数、每一帧播放的时间（每一帧并不一定相等，由
    GifBean *gifBean = static_cast<GifBean *>(malloc(sizeof(GifBean)));

    // 清空内存地址
    memset(gifBean,0, sizeof(GifBean));
    gifFileType->UserData = gifBean;
    gifBean->dealys = static_cast<int *>(malloc(sizeof(int) * gifFileType->ImageCount));
    gifBean->total_frame = gifFileType->ImageCount;
    gifBean->current_frame = 0;

    // 遍历每一帧   图形控制扩展块  --延迟时间  ///Delay Time - 单位1/100秒 10ms
    for (int i = 0; i < gifBean->total_frame; ++i) {
        SavedImage frame = gifFileType->SavedImages[i];
        ExtensionBlock *extensionBlock;
        for (int j = 0; j < frame.ExtensionBlockCount; ++j) {
            if (frame.ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {}
            extensionBlock = &frame.ExtensionBlocks[j];
            break;
        }
        if (extensionBlock) {
            // 延迟时间, 两个字节表示一个int    Bytes[2]高八位  Bytes[1]低八位
            int dealy = (extensionBlock->Bytes[2] << 8 | extensionBlock->Bytes[1]) * 10;
//            LOGE("nativeLoadPath 时间 %d",dealy);
            gifBean->dealys[i] = dealy;
        }
    }

    return (jlong) gifFileType;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_karashok_gifdemo_GifHandler_nativeGetWidth(
        JNIEnv* env,
        jobject instance,
        jlong gifAddr) {
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gifAddr);
    LOGE("nativeGetWidth %d",gifFileType->SWidth);
    return gifFileType->SWidth;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_karashok_gifdemo_GifHandler_nativeGetHeight(
        JNIEnv* env,
        jobject instance,
        jlong gifAddr) {
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gifAddr);
    LOGE("nativeGetHeight %d",gifFileType->SHeight);
    return gifFileType->SHeight;
}

void drawFrame(GifFileType *gifFileType, GifBean *gifBean, AndroidBitmapInfo info, void *pixels)  {
    SavedImage savedImage = gifFileType->SavedImages[gifBean->current_frame];
    GifImageDesc frameInfo = savedImage.ImageDesc;
    // 整幅Bitmap的首地址
    int *px = (int*)pixels;
    int *line;
    ColorMapObject *colorMapObject = frameInfo.ColorMap;
    px = (int*)((char*)px + info.stride * frameInfo.Top);
    GifByteType gifByteType;
    GifColorType gifColorType;

    // 先遍历行  内容的区域
    for (int y = frameInfo.Top; y < frameInfo.Top + frameInfo.Height; ++y) {
        line = px;
        for (int x = frameInfo.Left; x < frameInfo.Left + frameInfo.Width; ++x) {

            // 索引
            int pointPixel = (y - frameInfo.Top) * frameInfo.Width + (x - frameInfo.Left);

            // 当前帧的像素数据   压缩  lzw算法
            gifByteType=savedImage.RasterBits[pointPixel];

            // 字典
            gifColorType= colorMapObject->Colors[gifByteType];
            line[x] = argb(255, gifColorType.Red, gifColorType.Green, gifColorType.Blue);
        }
        px = (int *) ((char*)px + info.stride);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_karashok_gifdemo_GifHandler_nativeUpdateFrame(
        JNIEnv* env,
        jobject instance,
        jlong gifAddr,
        jobject bitmap) {
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gifAddr);
    GifBean *gifBean = static_cast<GifBean *>(gifFileType->UserData);

    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env,bitmap,&info);

    void *pixels;

    AndroidBitmap_lockPixels(env,bitmap,&pixels);
    drawFrame(gifFileType,gifBean,info,pixels);
    gifBean->current_frame +=1;
    LOGE("nativeUpdateFrame 当前帧  %d",gifBean->current_frame);
    if (gifBean->current_frame >= gifBean->total_frame - 1) {
        gifBean->current_frame = 0;
    }
    AndroidBitmap_unlockPixels(env,bitmap);

    return gifBean->dealys[gifBean->current_frame];
}

