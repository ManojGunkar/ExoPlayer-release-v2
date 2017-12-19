//
// Created by Adarsh on 28/11/17.
//

#ifndef BOOMANDROID_CONTEXT_H
#define BOOMANDROID_CONTEXT_H

#include <jni.h>

namespace gdpl {

    class Context {
    public:

        Context(JNIEnv* env, jobject object) : _env(env), _object(object) {
            init(_env,_object);
        }

        jobject getPackageManager() const {
            return _env->CallObjectMethod(_object, sMethodId_getPackageManager);
        }

        jobject getAssets() const {
            return _env->CallObjectMethod(_object, sMethodId_getAssets);
        }

        jstring getPackageName() const {
            return static_cast<jstring>(_env->CallObjectMethod(_object, sMethodId_getPackageName));
        }

    private:

        static void init(JNIEnv* env,  jobject obj);

    private:

        JNIEnv* _env;
        jobject _object;

        static jmethodID sMethodId_getPackageManager;
        static jmethodID sMethodId_getAssets;
        static jmethodID sMethodId_getPackageName;

        static bool sIsInitialized;
    };

}


#endif //BOOMANDROID_CONTEXT_H
