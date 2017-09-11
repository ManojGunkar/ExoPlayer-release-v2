//
// Created by Adarsh on 27/06/17.
//

#ifndef EXOPLAYER_RELEASE_V2_BYTEBUFFER_H
#define EXOPLAYER_RELEASE_V2_BYTEBUFFER_H

#include <jni.h>
#include <stdint.h>

namespace gdpl {

    class ByteBuffer {
    public:
        ByteBuffer(JNIEnv* env, jobject obj) : _env(env), _object(obj) {
            init(env, obj);
        }

        void limit(int lim) {
            _env->CallObjectMethod(_object, limit_set_id, lim);
        }

        int limit() const {
            return _env->CallIntMethod(_object, limit_id);
        }

        void position(int pos) {
            _env->CallObjectMethod(_object, position_set_id, pos);
        }

        int position() const {
            return _env->CallIntMethod(_object, position_id);
        }

        long capacity() {
            return _env->GetDirectBufferCapacity(_object);
        }

        uint8_t* bytes() {
            return (uint8_t*)_env->GetDirectBufferAddress(_object);
        }

    private:
        JNIEnv* _env;
        jobject _object;

        static void init(JNIEnv* env,  jobject obj) {
            if ( initialized ) return;
            jclass cls = env->GetObjectClass(obj);
            limit_set_id = env->GetMethodID(cls, "limit", "(I)Ljava/nio/Buffer;");
            position_set_id = env->GetMethodID(cls, "position", "(I)Ljava/nio/Buffer;");
            limit_id = env->GetMethodID(cls, "limit", "()I");
            position_id = env->GetMethodID(cls, "position", "()I");
            initialized = true;
        }

        static bool initialized;
        static jmethodID limit_id, limit_set_id;
        static jmethodID position_id, position_set_id;
    };

}


#endif //EXOPLAYER_RELEASE_V2_BYTEBUFFER_H
