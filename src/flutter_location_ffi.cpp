#include "flutter_location_ffi.h"

static JavaVM *g_javaVm = nullptr;
static jclass g_plugin = nullptr;

extern "C" {
JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnvAttachGuard jniEnvAttachGuard(vm);
    JNIEnv *env = jniEnvAttachGuard;

    JNILocalRefGuard localRefGuard(env, env->FindClass(
            "dev/jonathanvegasp/flutter_location_ffi/FlutterLocationFfiPlugin"));

    jobject plugin = localRefGuard;
    g_plugin = reinterpret_cast<jclass>(env->NewGlobalRef(plugin));
    g_javaVm = vm;

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *, void *) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;

    if (!env) {
        g_plugin = nullptr;
        g_javaVm = nullptr;
        return;
    }

    env->DeleteGlobalRef(g_plugin);
    g_plugin = nullptr;
    g_javaVm = nullptr;
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_check_and_request_permission(Callback callback) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;
    ResultChannelInstanceGuard channelInstanceGuard(env, callback);
    jobject instance = channelInstanceGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "checkAndRequestPermission",
                                                  "(Ldev/jonathanvegasp/result_channel/ResultChannel;)V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1, instance);
}

FFI_PLUGIN_EXPORT ResultNative *flutter_location_ffi_check_permission(void) {
    JNIEnvAttachGuard envAttachGuard(g_javaVm);
    JNIEnv *env = envAttachGuard;
    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "checkPermission", "()[B");
    JNILocalRefGuard localRefGuard(env, env->CallStaticObjectMethod(g_plugin,
                                                                    jmethodID1));

    auto bytes = reinterpret_cast<jbyteArray>(localRefGuard.get());

    return JavaByteArrayGuard(ResultChannelStatusOk, env, bytes);
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_get_current(Callback callback) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;
    ResultChannelInstanceGuard channelInstanceGuard(env, callback);
    jobject instance = channelInstanceGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "getCurrent",
                                                  "(Ldev/jonathanvegasp/result_channel/ResultChannel;)V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1, instance);
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_start_updates(Callback callback) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;
    ResultChannelInstanceGuard channelInstanceGuard(env, callback);
    jobject instance = channelInstanceGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "startUpdates",
                                                  "(Ldev/jonathanvegasp/result_channel/ResultChannel;)V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1, instance);
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_stop_updates(void) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "stopUpdates", "()V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1);
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_open_app_settings(void) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "openAppSettings", "()V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1);
}

FFI_PLUGIN_EXPORT void flutter_location_ffi_open_location_settings(void) {
    JNIEnvAttachGuard jniEnvAttachGuard(g_javaVm);
    JNIEnv *env = jniEnvAttachGuard;

    jmethodID jmethodID1 = env->GetStaticMethodID(g_plugin, "openLocationSettings", "()V");

    env->CallStaticVoidMethod(g_plugin, jmethodID1);
}
}
