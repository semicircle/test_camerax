#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "opencv2/highgui/highgui.hpp"
#include <vector>
#include <android/bitmap.h>
#include <android/log.h>

using namespace std;
using namespace cv;

#define  LOG_TAG    "CameraPreviewSurfaceView_Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {

#if 0
JNIEXPORT void JNICALL Java_com_doubleloop_camerax_CameraPreviewSurfaceView_FindFeatures(JNIEnv* env, jobject thiz, jlong addrGray, jlong addrRgba)
{
    Mat* pMatGr=(Mat*)addrGray;
    Mat* pMatRgb=(Mat*)addrRgba;
    vector<KeyPoint> v;
#if 0
    FastFeatureDetector detector(50);
    detector.detect(*pMatGr, v);
    for( size_t i = 0; i < v.size(); i++ )
        circle(*pMatRgb, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(255,0,0,255));
#endif

    cvtColor( *pMatGr, *pMatRgb, CV_GRAY2RGBA, 4);
}

#endif

typedef struct {
	cv::VideoCapture* pCapture;
	cv::Mat* pMatGrey;
	cv::Mat* pMatGrey2;
	cv::Mat* pMatRGBA;
	int width;
	int height;
} stru_camerax_state;

stru_camerax_state state;

/* ??? */
//extern int AndroidBitmap_lockPixels(JNIEnv* env, jobject jbitmap, void** addrPtr);
//extern int AndroidBitmap_unlockPixels(JNIEnv* env, jobject jbitmap);

JNIEXPORT jint JNICALL Java_com_doubleloop_camerax_CameraPreviewSurfaceView_NativeInit(JNIEnv* env, jobject thiz, jobject camera, jint width, jint height)
{
	LOGI("Java_com_doubleloop_camerax_CameraPreviewSurfaceView_NativeInit");

	/* init Camera */
	//state.pCapture = (cv::VideoCapture*)camera;
	state.pCapture = (cv::VideoCapture*)new cv::VideoCapture(0);
	state.pCapture->set(CV_CAP_PROP_FRAME_WIDTH, width);
	state.pCapture->set(CV_CAP_PROP_FRAME_HEIGHT, height);


	/* init Mats */
	state.pMatGrey = (cv::Mat*)new Mat();
	state.pMatRGBA = (cv::Mat*)new Mat();
	state.pMatGrey2 = (cv::Mat*)new Mat();

	state.width = width;
	state.height = height;

	return 0;
}

JNIEXPORT void JNICALL Java_com_doubleloop_camerax_CameraPreviewSurfaceView_NativeProcessFrame(JNIEnv* env, jobject thiz, jobject bitmap, jint mode)
{
	void*              pixels = 0;

	/* tip: 1. grab,
	 * 2. retrieve.
	 * 3. process.
	 * 4. cvt to bitmap.
	 */
	try {
		//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
		state.pCapture->grab();
		//LOGI("ProcessFrame 1.5");
		state.pCapture->retrieve(*state.pMatGrey, CV_CAP_ANDROID_GREY_FRAME);
		//LOGI("ProcessFrame 2");
		//threshold(*state.pMatGrey, *state.pMatGrey2, 0, 0, 1);
		//cvtColor(*state.pMatGrey2, *state.pMatRGBA, CV_GRAY2RGBA, 4);
		//cvtColor(*state.pMatGrey, *state.pMatRGBA, CV_GRAY2RGBA, 4);
		//LOGI("ProcessFrame 3");
		blur(*state.pMatGrey, *state.pMatGrey2, Size(3,3));
		Canny(*state.pMatGrey, *state.pMatGrey, 80, 100, 3);


		//TODO: seems pMatRGBA can be deleted.

		CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
		CV_Assert( pixels );
		//LOGI("ProcessFrame 4");

		Mat tmp(state.height, state.width, CV_8UC4, pixels);
		cvtColor(*state.pMatGrey, tmp, CV_GRAY2RGBA, 4);
		//state.pMatRGBA->copyTo(tmp);
		//LOGI("ProcessFrame 5");

		AndroidBitmap_unlockPixels(env, bitmap);
	} catch (...) {
		AndroidBitmap_unlockPixels(env, bitmap);
		jclass je = env->FindClass("com/doubleloop/camerax/CameraPreviewSurfaceView");
		env->ThrowNew(je, "Unknown exception in JNI code {NativeProcessFrame}");
	}

	return;
}

JNIEXPORT void JNICALL Java_com_doubleloop_camerax_CameraPreviewSurfaceView_NativeDestory(JNIEnv* env, jobject thiz)
{
	state.pCapture->release();
	return;
}



}/* extern "C" */

