#include <jni.h>
#include <string>
#include "include/opencv2/opencv.hpp"
#include "FaceAlignment/include/common.h"
#include "FaceTrack.h"
#include "include/opencv2/imgproc/types_c.h"

using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_karashok_opengldemo_MainActivity_stringFromJNI(
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

extern "C" JNIEXPORT jlong JNICALL
Java_com_karashok_opengldemo_utils_face_FaceTrack_nativeCreate(
        JNIEnv* env,
        jobject instance,
        jstring modelStr,
        jstring seetaStr) {
    const char *model = env->GetStringUTFChars(modelStr,0);
    const char *seeta = env->GetStringUTFChars(seetaStr,0);

    FaceTrack *faceTrack = new FaceTrack(model,seeta);

    env->ReleaseStringUTFChars(modelStr,model);
    env->ReleaseStringUTFChars(seetaStr,seeta);

    return reinterpret_cast<jlong>(faceTrack);
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opengldemo_utils_face_FaceTrack_nativeStart(
        JNIEnv* env,
        jobject instance,
        jlong self) {
    if (self == 0) {
        return;
    }
    FaceTrack *me = (FaceTrack*)self;
    me->startTracking();
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_opengldemo_utils_face_FaceTrack_nativeStop(
        JNIEnv* env,
        jobject instance,
        jlong self) {
    if (self == 0) {
        return;
    }
    FaceTrack *me = (FaceTrack*)self;
    me->stopTracking();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_karashok_opengldemo_utils_face_FaceTrack_nativeDetector(
        JNIEnv* env,
        jobject instance,
        jlong self,
        jbyteArray dataArr,
        jint cameraId,
        jint width,
        jint height) {
    if (self == 0) {
        return NULL;
    }

    jbyte *data = env->GetByteArrayElements(dataArr,NULL);
    FaceTrack *me = (FaceTrack*)self;
    Mat src(height + height / 2, width, CV_8UC1, data);

    cvtColor(src,src,CV_YUV2RGBA_NV21);
    if (cameraId == 1) {
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
        flip(src,src,1);
    } else {
        rotate(src,src,ROTATE_90_CLOCKWISE);
    }

    cvtColor(src,src,COLOR_RGBA2GRAY);
    equalizeHist(src,src);

    vector<Rect2f> rects;
    me->detector(src,rects);
    env->ReleaseByteArrayElements(dataArr,data,0);

    int w = src.cols;
    int h = src.rows;
    src.release();
    int ret = rects.size();
    if (ret) {
        jclass cls = env->FindClass("com/karashok/opengldemo/utils/face/FaceData");
        jmethodID construct = env->GetMethodID(cls,"<init>","([FIIII)V");
        int size = ret * 2;
        jfloatArray jfa = env->NewFloatArray(size);
        for (int i = 0, j = 0; i < size; j++) {
            float  f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(jfa,i,2,f);
            i += 2;
        }
        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        jobject faceData = env->NewObject(cls,construct,jfa,width,height,w,h);
        return faceData;
    }
    return NULL;
}