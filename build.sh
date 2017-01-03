#!/bin/sh

ROOT_DIR=$(pwd)
BUILD_TOOLS="${ANDROID_HOME}/build-tools/24.0.3"
APK_DIR=app/build/outputs/apk

check_error() {
	if [ $1 -ne 0 ]; then
		echo "Build failed!"
		exit $1
	fi
}


# Clean build directory
if [ -d "$BUILD_DIR" ]; then
	rm -rdf $BUILD_DIR/*.*
else
	mkdir -p "$BUILD_DIR"
fi


cd BoomAndroid
chmod +x gradlew

./gradlew assembleRelease
check_error $?

$BUILD_TOOLS/zipalign -v -p 4 "${APK_DIR}/app-release-unsigned.apk" "${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner sign --ks keystore.jks \
	--ks-pass pass:BoomAndroid \
	--out "${BUILD_DIR}/$APK_NAME" \
	"${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?


cd "$ROOT_DIR"