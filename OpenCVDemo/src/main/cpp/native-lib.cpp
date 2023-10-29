#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"
#include <android/native_window_jni.h>
#include "CascadeDetectorAdapter.h"
#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"DemoOpenCVJIN",__VA_ARGS__)

using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_karashok_opencvdemo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "OpenCV Version : ";
    hello.append(std::to_string(CV_VERSION_MAJOR));
    hello.append(".");
    hello.append(std::to_string(CV_VERSION_MINOR));
    hello.append(".");
    hello.append(std::to_string(CV_VERSION_REVISION));
    return env->NewStringUTF(hello.c_str());
}

ANativeWindow *window = 0;
DetectionBasedTracker *tracker = 0;

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opencvdemo_OpenCVActivity_init(
        JNIEnv* env,
        jobject instance,
        jstring modelPath) {
    const char *model = env->GetStringUTFChars(modelPath,0);
    if (tracker) {
        tracker->stop();
        delete tracker;
        tracker = 0;
    }

    // 智能指针
    Ptr<CascadeClassifier> mainClassifier = makePtr<CascadeClassifier>(model);

    // 创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(mainClassifier);

    Ptr<CascadeClassifier> trackingClassifier = makePtr<CascadeClassifier>(model);
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(trackingClassifier);

    // 拿去用的跟踪器
    DetectionBasedTracker::Parameters DetectionParameters;
    tracker = new DetectionBasedTracker(mainDetector,trackingDetector,DetectionParameters);

    // 开启跟踪器
    tracker->run();
    env->ReleaseStringUTFChars(modelPath,model);
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opencvdemo_OpenCVActivity_setSurface(
        JNIEnv* env,
        jobject instance,
        jobject surface) {
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    window = ANativeWindow_fromSurface(env,surface);
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opencvdemo_OpenCVActivity_postData(
        JNIEnv* env,
        jobject instance,
        jbyteArray dataArr,
        jint w,
        jint h,
        jint cameraId) {
    // NV21 的数据
    jbyte *data = env->GetByteArrayElements(dataArr,NULL);

    // 高、宽
    Mat src(h * 3 / 2,w,CV_8UC1,data);

    // 颜色格式的转换 NV21 -> RGBA
    cvtColor(src,src,COLOR_YUV2RGBA_NV21);

    // 输出文件
//    imwrite("/sdcard/src.jpg",src);

//    LOGE("before rows %d, cols %d",src.rows,src.cols);

    if (cameraId == 1) {
        // 前置摄像头需要逆时针旋转 90 度
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
        // 镜像
        flip(src,src,1);
    } else {
        // 后置摄像头顺时针旋转 90 度
        rotate(src,src,ROTATE_90_CLOCKWISE);
    }

//    LOGE("after rows %d, cols %d",src.rows,src.cols);

    Mat gray;
    // 灰色
    cvtColor(src,gray,COLOR_RGBA2GRAY);

    // 增强对比度（直方图均衡）
    equalizeHist(gray,gray);

    std::vector<Rect> faces;

    // 定位人脸
    tracker->process(gray);
    tracker->getObjects(faces);
    for (Rect rect : faces) {
        // 画矩形
        rectangle(src,rect,Scalar(255,0,255));
    }

    // 显示
    if (window) {
        // 设置 Window 的属性。因为旋转了，所以宽、高需要交换
        // 这里使用 cols 和 rows 代表宽、高就不用关心上面是否旋转了
        ANativeWindow_setBuffersGeometry(window,src.cols,src.rows,WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer buffer;

        // lock 失败，直接 break 出去
        if (ANativeWindow_lock(window,&buffer,0)) {
            ANativeWindow_release(window);
            window = 0;
        }

        int srcLineSize = src.cols * 4;
        int dstLineSize = buffer.stride * 4;
        uint8_t *dstData = static_cast<uint8_t *> (buffer.bits);

        for (int i = 0; i < buffer.height; ++i) {
            memcpy(dstData + dstLineSize * i, src.data + srcLineSize * i, srcLineSize);
        }

        // 提交刷新
        ANativeWindow_unlockAndPost(window);
    }
    // 释放 Mat，内部采用引用计数
    src.release();
    gray.release();
    env->ReleaseByteArrayElements(dataArr,data,0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opencvdemo_OpenCVActivity_release(
        JNIEnv* env,
        jobject instance) {
    if (tracker) {
        tracker->stop();
        delete tracker;
        tracker = 0;
    }
}