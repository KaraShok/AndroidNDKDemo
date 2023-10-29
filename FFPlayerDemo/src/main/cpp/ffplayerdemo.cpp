#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_karashok_ffplayerdemo_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "FFPlayer Demo";
    return env->NewStringUTF(hello.c_str());
}