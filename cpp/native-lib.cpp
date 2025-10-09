#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "OpenCV_Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_edgedetectionviewer_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    std::string hello = "OpenCV Version: ";
    hello += CV_VERSION;

    LOGI("OpenCV initialized successfully!");
    LOGI("OpenCV Version: %s", CV_VERSION);

    return env->NewStringUTF(hello.c_str());
}

// ✅ New function: Apply Canny edge detection
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionviewer_ImageProcessorKt_nativeCannyEdgeDetection(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {

    try {
        // Get input Mat
        cv::Mat& inputMat = *(cv::Mat*)matAddr;

        // Create output Mat
        cv::Mat grayMat, edges;

        // Convert to grayscale
        cv::cvtColor(inputMat, grayMat, cv::COLOR_RGBA2GRAY);

        // Apply Gaussian blur to reduce noise
        cv::GaussianBlur(grayMat, grayMat, cv::Size(5, 5), 1.5);

        // Apply Canny edge detection
        double lowThreshold = 50.0;
        double highThreshold = 150.0;
        cv::Canny(grayMat, edges, lowThreshold, highThreshold);

        // Convert back to RGBA for display
        cv::Mat edgesRGBA;
        cv::cvtColor(edges, edgesRGBA, cv::COLOR_GRAY2RGBA);

        // Copy to input Mat (in-place modification)
        edgesRGBA.copyTo(inputMat);

        LOGD("Canny edge detection applied successfully");

        return (jlong) new cv::Mat(inputMat);

    } catch (const cv::Exception& e) {
        LOGE("OpenCV Exception: %s", e.what());
        return 0;
    } catch (const std::exception& e) {
        LOGE("Standard Exception: %s", e.what());
        return 0;
    }
}

// ✅ Grayscale conversion function
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionviewer_ImageProcessorKt_nativeGrayscale(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {

    try {
        cv::Mat& inputMat = *(cv::Mat*)matAddr;
        cv::Mat grayMat, grayRGBA;

        // Convert to grayscale
        cv::cvtColor(inputMat, grayMat, cv::COLOR_RGBA2GRAY);

        // Convert back to RGBA
        cv::cvtColor(grayMat, grayRGBA, cv::COLOR_GRAY2RGBA);

        grayRGBA.copyTo(inputMat);

        return (jlong) new cv::Mat(inputMat);

    } catch (const cv::Exception& e) {
        LOGE("Grayscale Exception: %s", e.what());
        return 0;
    }
}
