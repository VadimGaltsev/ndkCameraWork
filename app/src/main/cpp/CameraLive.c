#include <jni.h>
#include <android/bitmap.h>
#include <stdlib.h>

#if !defined(CAMERA_API)
#define CAMERA_API
#define toInteger(val) \
    (0xff & (int32_t) val)

#define max(val1, val2) \
    (val1 < val2) ? val2 : val1

#define clamp(val1, lowestVal, highestVal) \
    ((val1 < 0) ? lowestVal : (val1 > highestVal) ? highestVal : val1)

#define color(colorValR, colorValG, colorValB) \
    (0xFF000000 | ((colorValB << 6) & 0x00FF0000) \
        | ((colorValG >> 2) & 0x0000FF00) \
        | ((colorValR >> 10) & 0x000000FF))

#define class_path \
    "com/example/vadik/livecamerandk/MainActivity"

#define method_name \
    Java_com_example_vadik_livecamerandk_MainActivity_decode
#define method_name_str \
    "decode"
#define method_signature \
    "(Landroid/graphics/Bitmap;[BI)V"
#endif

/**
 * decoding data with android bitmap library
 * @first need to get information about image type,
 * check image data type
 * @second lock pixel data and get access for critical pixel section
 * @third convert YUV format to RGB
 *
 * @param env - jvm thread ptr
 * @param instance - object call ptr
 * @param imageB - bitmap ptr
 * @param data_ - byte array ptr with raw data
 * @param i - image color format
 */
JNIEXPORT void JNICALL Java_com_example_vadik_livecamerandk_MainActivity_decode(JNIEnv *env, jobject instance,
                                                                                jobject imageB, jbyteArray data_, jint __i) {
    /**
     * @first@
     */
    AndroidBitmapInfo _bitmapInfo;
    uint32_t * _bitmapContent;
    if (AndroidBitmap_getInfo(env,imageB, &_bitmapInfo) < 0) abort();
    if (_bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) abort();
    /**
    * @second
    */
    if (AndroidBitmap_lockPixels(env, imageB, (void**) &_bitmapContent) < 0) abort();
    jbyte * source = (*env)->GetPrimitiveArrayCritical(env, data_, JNI_FALSE);
    if (source == NULL) abort();
    /**
     * @third
     */
    int32_t _frameSize = _bitmapInfo.width * _bitmapInfo.height;
    int32_t _yIndex, _uvIndex, x, y;
    int32_t _colorY, _colorU, _colorV;
    int32_t _colorR, _colorG, _colorB;
    int32_t _y1192;
    // components UV in format each two steps has same ptr
    for (y = 0, _yIndex = 0; y < _bitmapInfo.height; ++y) {
        _colorU = 0; _colorV = 0;
        _uvIndex = _frameSize + (y >> 1) * _bitmapInfo.width;
        for (x = 0; x < _bitmapInfo.width; ++x, ++_yIndex) {
            /**
             * get YUV components. each horizontal 1 pair UV on 2 Y
             */
            _colorY = max(toInteger(source[_yIndex]) - 16, 0);
            if (!(x % 2)) {
                _colorU = toInteger(source[_uvIndex++]) - 128;
                _colorV = toInteger(source[_uvIndex++]) - 128;
            }

            _y1192 = 1192 * _colorY;
            _colorR = (_y1192 + 1634 * _colorV);
            _colorG = (_y1192 + 833 * _colorV - 400 * _colorU);
            _colorB = (_y1192 + 2066 * _colorU);

            _colorR = clamp(_colorR, 0, 262143);
            _colorG = clamp(_colorG, 0, 262143);
            _colorB = clamp(_colorB, 0, 262143);
            _bitmapContent[_yIndex] = color(_colorR, _colorG, _colorB);
            _bitmapContent[_yIndex] &= __i;
        }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, data_, source, JNI_FALSE);
    if (AndroidBitmap_unlockPixels(env, imageB) < 0) abort();
}
JNINativeMethod _methodRegister[] = {
        {method_name_str, method_signature, (void *) method_name}
};

void _exceptionOnCall(JNIEnv * env) {
    if ((*env)->ExceptionCheck(env)){
        (*env)->ExceptionDescribe(env);
    }
}

/**
 * Here we can get and env pointer and find needed classes for native code
 * in this case we make register out method on system load out native library
 * to speed up this process in java vm3
 *
 * @param instance_VM
 * @param _ref
 * @return
 */
JNIEXPORT jint JNI_OnLoad(JavaVM * instance_VM, void * _ref) {
    JNIEnv * env;
    if ((*instance_VM)->GetEnv(instance_VM,(void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        abort();
    }
    jclass _CameraClass = (*env)->FindClass(env, class_path);
    if (_CameraClass == NULL) {
        _exceptionOnCall(env);
        abort();
    }
    jint res = (*env)->RegisterNatives(env, _CameraClass, _methodRegister, 1);
    if (res != 0) {
        _exceptionOnCall(env);
        abort();
    }
    (*env)->DeleteLocalRef(env, _CameraClass); _exceptionOnCall(env);
    return JNI_VERSION_1_6;
}




