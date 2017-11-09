#!/bin/sh

ROOT_DIR=$(pwd)
BUILD_TOOLS="${ANDROID_HOME}/build-tools/24.0.3"
APK_DIR=app/build/outputs/apk
MAPPING_DIR=app/build/outputs/mapping
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
APK_DIR=${APK_DIR}/${FLAVOUR}/release
MAPPING_DIR=${MAPPING_DIR}/${FLAVOUR}/release

./gradlew assemble${FLAVOUR}Release -PbuildNumber=$BUILD_NUMBER
check_error $?


$BUILD_TOOLS/zipalign -v -p 4 "${APK_DIR}/app-${FLAVOUR}-release-unsigned.apk" "${APK_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner sign --ks keystore.jks \
	--ks-pass pass:BoomAndroid \
	--out "${APK_DIR}/$APK_NAME" \
	"${APK_DIR}/app-unsigned-aligned.apk"
check_error $?

$BUILD_TOOLS/apksigner verify "${APK_DIR}/$APK_NAME"
check_error $?

aapt dump badging "${APK_DIR}/$APK_NAME"

cp "${APK_DIR}/$APK_NAME" "${BUILD_DIR}"
cp ${MAPPING_DIR}/mapping.txt "${BUILD_DIR}"
cp ${MAPPING_DIR}/seeds.txt "${BUILD_DIR}"

ARTIFACT_DIR=$ROOT_DIR/build-output
if [ -f "$ARTIFACT_DIR" ]; then
	rm -rdf "$ARTIFACT_DIR"
fi

mkdir "$ARTIFACT_DIR"
cp -R "${BUILD_DIR}/" "$ARTIFACT_DIR"

cd "$ROOT_DIR"
