#!/bin/sh

ROOT_DIR = $(pwd)
BUILD_TOOLS="${ANDROID_HOME}/build-tools/24.0.3"
APK_DIR=app/build/outputs/apk
BUILD_DIR=$TMPDIR/boom

cd BoomAndroid

mkdir -p "$BUILD_DIR"
chmod +x gradlew
./gradlew assembleRelease
$BUILD_TOOLS/zipalign -v -p 4 "${APK_DIR}/app-release-unsigned.apk" "${BUILD_DIR}/app-unsigned-aligned.apk"
$BUILD_TOOLS/apksigner sign --ks keystore.jks \
	--ks-pass pass:BoomAndroid
	--out "../boom-release.apk" \
	"${BUILD_DIR}/app-unsigned-aligned.apk"

cd "$ROOT_DIR"