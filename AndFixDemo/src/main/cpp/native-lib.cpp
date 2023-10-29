#include <jni.h>
#include <string>
#include "art_method.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_karashok_andfixdemo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_karashok_andfixdemo_DexManager_replace(
        JNIEnv* env,
        jobject jobj,
        jobject wMethod,
        jobject rMethod) {

    art::mirror::ArtMethod *wrong = reinterpret_cast<art::mirror::ArtMethod*>(env->FromReflectedMethod(wMethod));

    wrong->access_flags_ = wrong->access_flags_ & (~0x0002) | 0x0001;

    art::mirror::ArtMethod *right = reinterpret_cast<art::mirror::ArtMethod*>(env->FromReflectedMethod(rMethod));

    wrong->declaring_class_ = right->declaring_class_;
    wrong->dex_cache_resolved_methods_ = right->dex_cache_resolved_methods_;
    wrong->access_flags_ = right->access_flags_;
    wrong->dex_cache_resolved_types_ = right->dex_cache_resolved_types_;
    wrong->dex_code_item_offset_ = right->dex_code_item_offset_;
    wrong->dex_method_index_ = right->dex_method_index_;
    wrong->method_index_ = right->method_index_;
}