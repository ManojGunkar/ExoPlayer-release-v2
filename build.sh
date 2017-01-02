#!/bin/sh

ROOT_DIR=$(pwd)
BUILD_TOOLS="${ANDROID_HOME}/build-tools/24.0.3"
APK_DIR=app/build/outputs/apk
BUILD_DIR=$TMPDIR/boom

check_error() {
	if [ $1 -ne 0 ]; then
		echo "Build failed!"
		exit $1
	fi
}


cd BoomAndroid

mkdir -p "$BUILD_DIR"
if [ -f "../boom-release.apk" ]; then
	rm -rdf "../boom-release.apk"
fi

chmod +x gradlew

./gradlew assembleRelease
check_error $?

$BUILD_TOOLS/zipalign -v -p 4 "${APK_DIR}/app-release-unsigned.apk" "${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner sign --ks keystore.jks \
	--ks-pass pass:BoomAndroid \
	--out "../boom-release.apk" \
	"${BUILD_DIR}/app-unsigned-aligned.apk"
check_error $?


cd "$ROOT_DIR"