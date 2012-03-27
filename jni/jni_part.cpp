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
		cv::Mat* pMatGrey3;
		cv::Mat* pMatGrey4;
		cv::Mat* pMatGrey5;
		cv::Mat* pMatGrey6;
		cv::Mat* pMatGreyFinal;
		cv::Mat* pMatRGBA;

		cv::Mat* pMatColorEdge;
		cv::Mat* pMatGreyEdge;
		cv::Mat* pMatImageGrey;
		cv::Mat* pMatImage;

		int width;
		int height;
	} stru_camerax_state;

	stru_camerax_state state;

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
		state.pMatGrey3 = (cv::Mat*)new Mat();
		state.pMatGrey4 = (cv::Mat*)new Mat();
		state.pMatGrey5 = (cv::Mat*)new Mat();
		state.pMatGrey6 = (cv::Mat*)new Mat();
		state.pMatGreyFinal = (cv::Mat*)new Mat();

		state.pMatColorEdge = new Mat();
		state.pMatGreyEdge = new Mat();
		state.pMatImageGrey = new Mat();
		state.pMatImage = new Mat();


		state.width = width;
		state.height = height;

		return 0;
	}

	JNIEXPORT void JNICALL Java_com_doubleloop_camerax_CameraPreviewSurfaceView_NativeProcessFrame(
		JNIEnv* env, jobject thiz, jobject bitmap, jint mode,
		jint parameter1, jint parameter2, jint parameter3)
	{
		void*              pixels = 0;

		/* tip: 1. grab,
		* 2. retrieve.
		* 3. process.
		* 4. cvt to bitmap.
		*/
		try {

			switch (mode) {
			case 0:
				{
					//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
					state.pCapture->grab();
					//LOGI("ProcessFrame 1.5");
					state.pCapture->retrieve(*state.pMatGrey, CV_CAP_ANDROID_GREY_FRAME);
					//LOGI("ProcessFrame 2");
					Sobel( *state.pMatGrey, *state.pMatGrey2, CV_16S, 1, 0, 3, 1, 0, BORDER_DEFAULT );
					convertScaleAbs( *state.pMatGrey2, *state.pMatGrey4 );
					Sobel( *state.pMatGrey, *state.pMatGrey3, CV_16S, 0, 1, 3, 1, 0, BORDER_DEFAULT );
					convertScaleAbs( *state.pMatGrey3, *state.pMatGrey5 );

					addWeighted( *state.pMatGrey4, 0.5, *state.pMatGrey5, 0.5, 0, *state.pMatGreyFinal );

					for( int i = 0; i < state.pMatGreyFinal->rows; ++i) {
						for( int j = 0; j < state.pMatGreyFinal->cols; ++j ) {
							state.pMatGreyFinal->at<uchar>(i,j) = 205 - state.pMatGreyFinal->at<uchar>(i,j);
						}
					}
					break;
				}
			case 1:
				{
					//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
					state.pCapture->grab();
					//LOGI("ProcessFrame 1.5");
					state.pCapture->retrieve(*state.pMatGrey, CV_CAP_ANDROID_GREY_FRAME);
					//LOGI("ProcessFrame 2");
					Sobel( *state.pMatGrey, *state.pMatGrey2, CV_16S, 1, 0, 3, 1, 0, BORDER_DEFAULT );
					convertScaleAbs( *state.pMatGrey2, *state.pMatGrey4 );
					Sobel( *state.pMatGrey, *state.pMatGrey3, CV_16S, 0, 1, 3, 1, 0, BORDER_DEFAULT );
					convertScaleAbs( *state.pMatGrey3, *state.pMatGrey5 );

					addWeighted( *state.pMatGrey4, 0.5, *state.pMatGrey5, 0.5, 0, *state.pMatGrey6 );

					for( int i = 0; i < state.pMatGrey6->rows; ++i) {
						for( int j = 0; j < state.pMatGrey6->cols; ++j ) {
							state.pMatGrey6->at<uchar>(i,j) = 255 - (state.pMatGrey6->at<uchar>(i,j));
						}
					}

					addWeighted( *state.pMatGrey6, 0.7, *state.pMatGrey, 0.3, 0, *state.pMatGreyFinal );
					break;
				}
			case 2:
				{
					// Canny with color
					//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
					state.pCapture->grab();
					//LOGI("ProcessFrame 1.5");
					state.pCapture->retrieve(*state.pMatImage, CV_CAP_ANDROID_COLOR_FRAME_BGRA);
					//state.pCapture->retrieve(*state.pMatImageGrey, CV_CAP_ANDROID_GREY_FRAME);
					cvtColor(*state.pMatImage, *state.pMatImageGrey, CV_BGRA2GRAY);
					blur(*state.pMatImageGrey, *state.pMatGreyEdge, Size(3,3));

					double edgeThresh = (double)parameter1;
					Canny(*state.pMatGreyEdge, *state.pMatGreyEdge, edgeThresh, edgeThresh*2, 3);
					*state.pMatColorEdge = Scalar::all(0);
					state.pMatImage->copyTo(*state.pMatColorEdge, *state.pMatGreyEdge);

					break;
				}
			case 3:
				{
					// Canny with color
					//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
					state.pCapture->grab();
					//LOGI("ProcessFrame 1.5");
					state.pCapture->retrieve(*state.pMatImage, CV_CAP_ANDROID_COLOR_FRAME_BGRA);
					//state.pCapture->retrieve(*state.pMatImageGrey, CV_CAP_ANDROID_GREY_FRAME);
					cvtColor(*state.pMatImage, *state.pMatImageGrey, CV_BGRA2GRAY);
					blur(*state.pMatImageGrey, *state.pMatGreyEdge, Size(3,3));

					//double edgeThresh2 = 2;
					Canny(*state.pMatGreyEdge, *state.pMatGreyEdge, 4, 6, 3);

					for( int i = 0; i < state.pMatColorEdge->rows; ++i) {
						for( int j = 0; j < state.pMatColorEdge->cols; ++j ) {
							state.pMatColorEdge->at<uchar>(i,j) = 255;
						}
					}

					state.pMatImage->copyTo(*state.pMatColorEdge, *state.pMatGreyEdge);

					break;
				}
			case 4:
				{
					// Canny with color
					//LOGI("ProcessFrame 1, pCapture: %u", (unsigned int)state.pCapture);
					state.pCapture->grab();
					//LOGI("ProcessFrame 1.5");
					state.pCapture->retrieve(*state.pMatImage, CV_CAP_ANDROID_COLOR_FRAME_BGRA);
					//state.pCapture->retrieve(*state.pMatImageGrey, CV_CAP_ANDROID_GREY_FRAME);
					cvtColor(*state.pMatImage, *state.pMatImageGrey, CV_BGRA2GRAY);
					//blur(*state.pMatImageGrey, *state.pMatGreyEdge, Size(3,3));



					double edgeThresh = (double)parameter1;
					Canny(*state.pMatImageGrey, *state.pMatImageGrey, edgeThresh, edgeThresh*2, 3);

					int erosion_size = parameter2 / 10;
					Mat element = getStructuringElement( MORPH_ELLIPSE,
															Size( 2*erosion_size + 1, 2*erosion_size+1 ),
															Point( erosion_size, erosion_size ) );

					erode( *state.pMatImageGrey, *state.pMatGreyEdge, element );

					*state.pMatColorEdge = Scalar::all(0);
					state.pMatImage->copyTo(*state.pMatColorEdge, *state.pMatGreyEdge);

					break;
				}
			}

			if( 2 == mode || 3 == mode || 4 == mode ) {
				CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
				CV_Assert( pixels );
				//LOGI("ProcessFrame 4");
				Mat tmp(state.height, state.width, CV_8UC4, pixels);

				//state.pMatImageGrey->copyTo(tmp); // Why this is not working?
				cvtColor(*state.pMatColorEdge, tmp, CV_BGR2RGBA, 4);

				AndroidBitmap_unlockPixels(env, bitmap);
			} else {
				CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
				CV_Assert( pixels );
				//LOGI("ProcessFrame 4");

				Mat tmp(state.height, state.width, CV_8UC4, pixels);
				cvtColor(*state.pMatGreyFinal, tmp, CV_GRAY2RGBA, 4);
				AndroidBitmap_unlockPixels(env, bitmap);
			}


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

