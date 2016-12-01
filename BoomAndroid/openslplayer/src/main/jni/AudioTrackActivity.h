/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <android/asset_manager.h>
#include "audioFx/AudioEngine.h"
#include "logger/log.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <stdio.h>
#include <assert.h>
#include <pthread.h>
#include <stdlib.h>
#include "android/asset_manager_jni.h"

#ifndef AUDIOTRACKACTIVITY_H_
#define AUDIOTRACKACTIVITY_H_

#ifdef __cplusplus
extern "C" {
#endif
namespace gdpl {
/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createEngine
 * Signature: ()V
 */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_createEngine
            (JNIEnv *, jclass, jobject, jint sampleRate, jint frameCount);

    /*
    * Class:     com_globaldelight_boomplayer_OpenSLPlayer
    * Method:    releaseEngine
    * Signature: ()V
    */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_releaseEngine
            (JNIEnv *, jclass);


/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createAudioPlayer
 * Signature: (III)Z
 */
    jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_createAudioPlayer(JNIEnv *, jclass ,
                                                                          jint , jint, jint );

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    write
 * Signature: (Z)V
 */
    jint Java_com_globaldelight_boomplayer_OpenSLPlayer_write(JNIEnv *, jobject, jobject, jint,
                                                          jint);


/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    setPlayingAudioPlayer
 * Signature: (Z)V
 */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_setPlayingAudioPlayer
            (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    seekTo
 * Signature: (I)V
 */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_seekTo(JNIEnv *, jclass, jlong);

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    setVolumeAudioPlayer
 * Signature: (I)V
 */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_setVolumeAudioPlayer
            (JNIEnv *, jclass, jint);

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    setMutAudioPlayer
 * Signature: (Z)V
 */
    void Java_com_globaldelight_boomplayer_OpenSLPlayer_setMutAudioPlayer
            (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    shutdown
 * Signature: (Z)Z
 */
    jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_shutdown
            (JNIEnv *, jclass, jboolean);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableAudioEffect(JNIEnv *env, jclass clazz,
                                                                  jboolean enabled);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_enable3DAudio(JNIEnv *env, jclass clazz,
                                                                  jboolean enabled);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableEqualizer(JNIEnv *env, jclass clazz,
                                                                    jboolean enabled);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableSuperBass(JNIEnv *env, jobject instance,
                                                                    jboolean enable);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableHighQuality(JNIEnv *env, jobject instance,
                                                                      jboolean enable);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_setIntensity(JNIEnv *env, jobject instance,
                                                                    jdouble value);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetEqualizer(JNIEnv *env, jobject instance, jint id,
                                                                 jfloatArray bandGains_);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetSpeakerState(JNIEnv *env, jobject instance,
                                                                    jint speakerId, jfloat value);

    jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_Get3DAudioState(JNIEnv *env, jobject instance);

    jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEffectsState(JNIEnv *env, jobject instance);

    jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetIntensity(JNIEnv *env, jobject instance);

    jint Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEqualizerId(JNIEnv *env, jobject instance);

    jfloat Java_com_globaldelight_boomplayer_OpenSLPlayer_GetSpeakerState(JNIEnv *env, jobject instance,
                                                                      jint speakerId);

    void Java_com_globaldelight_boomplayer_OpenSLPlayer_setHeadphoneType(JNIEnv *env, jobject instance,
                                                                          jint headphoneType);
    jint Java_com_globaldelight_boomplayer_OpenSLPlayer_getHeadphoneType(JNIEnv *env, jobject instance);
};
#ifdef __cplusplus
}
#endif
#endif
