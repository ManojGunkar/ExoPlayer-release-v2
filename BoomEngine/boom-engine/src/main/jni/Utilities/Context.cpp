//
// Created by Adarsh on 28/11/17.
//

#include "Context.h"

using namespace gdpl;

jmethodID Context::sMethodId_getPackageManager;
jmethodID Context::sMethodId_getAssets;
jmethodID Context::sMethodId_getPackageName;
bool Context::sIsInitialized = false;


void Context::init(JNIEnv* env,  jobject obj) {
    if ( sIsInitialized ) return;

    jclass cls = env->GetObjectClass(obj);

    sMethodId_getPackageManager = env->GetMethodID(cls, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    sMethodId_getAssets = env->GetMethodID(cls, "getAssets", "()Landroid/content/res/AssetManager;");
    sMethodId_getPackageName = env->GetMethodID(cls, "getPackageName", "()Ljava/lang/String;");

    sIsInitialized = true;
}
