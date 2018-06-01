#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_things_arc_com_hyperlpr_1things_1v1_WelcomeThingsActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
