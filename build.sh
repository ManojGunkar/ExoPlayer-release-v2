#!/bin/sh

ROOT_DIR=$(pwd)
BUILD_TOOLS="${ANDROID_HOME}/build-tools/24.0.3"
APK_DIR=app/build/outputs/apk
EXPIRY_DATE=$(date -v+1m '+"%d-%m-%Y"')

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

FLAVOUR=$1

./gradlew assemble${FLAVOUR}Release -PbuildNumber=$BUILD_NUMBER
check_error $?

$BUILD_TOOLS/zipalign -v -p 4 "${APK_DIR}/app-${FLAVOUR}-release-unsigned.apk" "${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner sign --ks keystore.jks \
	--ks-pass pass:BoomAndroid \
	--out "${BUILD_DIR}/$APK_NAME" \
	"${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner verify "${BUILD_DIR}/$APK_NAME"
check_error $?

aapt dump badging "${BUILD_DIR}/$APK_NAME"

cd "$ROOT_DIR"