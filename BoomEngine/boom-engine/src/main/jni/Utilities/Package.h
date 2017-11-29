//
// Created by Adarsh on 28/11/17.
//

#ifndef BOOMANDROID_PACKAGE_H
#define BOOMANDROID_PACKAGE_H

#include <jni.h>
#include <string>
#include <sstream>
#include <iomanip>
#include "Context.h"

namespace gdpl {

    class Package {
    public:
        Package(JNIEnv* env, Context& context):
                _env(env),
                _context(context),
                _packageManager(_context.getPackageManager())
        {

        }

        std::string getFingerPrint()
        {
            jbyteArray sig = getSignature();
            jobject cert = createCertificate(sig);
            jbyteArray key = getPublicKey(cert);
            return toHexString(key);
        }

    private:

        jbyteArray getSignature()
        {
            jstring name = _env->NewStringUTF("X509");

            jclass cls = _env->GetObjectClass(_packageManager);
            jmethodID mid_getPackageInfo = _env->GetMethodID(cls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
            jobject pkgInfo = _env->CallObjectMethod(_packageManager, mid_getPackageInfo, _context.getPackageName(), 0x00000040);

            jclass clsPackageInfo = _env->GetObjectClass(pkgInfo);
            jfieldID fid_signatures = _env->GetFieldID(clsPackageInfo, "signatures", "[Landroid/content/pm/Signature;");
            jobjectArray signatures = static_cast<jobjectArray>(_env->GetObjectField(pkgInfo, fid_signatures));

            jobject signature = _env->GetObjectArrayElement(signatures,0);
            jclass clsSignature = _env->GetObjectClass(signature);
            jmethodID mid_toByteArray = _env->GetMethodID(clsSignature, "toByteArray", "()[B");
            return static_cast<jbyteArray>(_env->CallObjectMethod(signature, mid_toByteArray));
        }

        jobject createInputStream(jbyteArray data)
        {
            jclass clsInputStream = _env->FindClass("java/io/ByteArrayInputStream");
            jmethodID mid_contructor = _env->GetMethodID(clsInputStream, "<init>", "([B)V");
            return _env->NewObject(clsInputStream, mid_contructor, data);
        }

        jobject createCertificate(jbyteArray data)
        {
            jstring name = _env->NewStringUTF("X509");

            jclass clsCertificateFactory = _env->FindClass("java/security/cert/CertificateFactory");
            jmethodID mid_getInstance = _env->GetStaticMethodID(clsCertificateFactory, "getInstance", "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");
            jmethodID mid_generateCertificate = _env->GetMethodID(clsCertificateFactory, "generateCertificate", "(Ljava/io/InputStream;)Ljava/security/cert/Certificate;");


            jobject certFactory = _env->CallStaticObjectMethod(clsCertificateFactory, mid_getInstance, name);
            return _env->CallObjectMethod(certFactory, mid_generateCertificate, createInputStream(data));
        }

        jbyteArray getPublicKey(jobject certificate)
        {
            jclass clsMessageDigest = _env->FindClass("java/security/MessageDigest");
            jmethodID mid_getInstance = _env->GetStaticMethodID(clsMessageDigest, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;");
            jmethodID mid_digest = _env->GetMethodID(clsMessageDigest, "digest", "([B)[B");

            jclass clsCertificate = _env->GetObjectClass(certificate);
            jmethodID mid_getEncoded = _env->GetMethodID(clsCertificate, "getEncoded", "()[B");

            jbyteArray encodedData = static_cast<jbyteArray>(_env->CallObjectMethod(certificate, mid_getEncoded));

            jobject digest = _env->CallStaticObjectMethod(clsMessageDigest, mid_getInstance, _env->NewStringUTF("SHA1"));
            return static_cast<jbyteArray>(_env->CallObjectMethod(digest,mid_digest,encodedData));
        }


        std::string toHexString(jbyteArray data)
        {
            jbyte* bytes = _env->GetByteArrayElements(data, nullptr);
            jint length = _env->GetArrayLength(data);

            std::stringstream ss;
            ss << std::uppercase << std::hex << std::setfill('0');
            for (int i = 0; i < length ; ++i) {
                ss  << std::setw(2) << static_cast<char>(bytes[i]);
                if ( i < length - 1 ) {
                    ss << ':';
                }
            }

            _env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
            std::string output = ss.str();
            return output;
        }


    private:
        JNIEnv* _env;
        Context& _context;
        jobject _packageManager;

    };

}


#endif //BOOMANDROID_PACKAGE_H
