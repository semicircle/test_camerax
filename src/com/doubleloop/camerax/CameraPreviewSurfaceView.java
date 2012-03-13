package com.doubleloop.camerax;

import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreviewSurfaceView implements SurfaceHolder.Callback, Runnable {
	private static final String TAG = "Sample::SurfaceView";

    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;
    
    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    int mFrameWidth;
    int mFrameHeight;
    
    private long mGraySubmatNativeAddr;
    private long mRgbaNativeAddr;

	public CameraPreviewSurfaceView(SurfaceHolder holder) {
		mHolder = holder;
        //mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
        mFrameWidth = 0;
        mFrameHeight = 0;
	}

	

	@Override
	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceCreated");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {
                Log.i(TAG, "before mCamera.getSupportedPreviewSizes()");
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                Log.i(TAG, "after mCamera.getSupportedPreviewSizes()");
                mFrameWidth = width;
                mFrameHeight = height;

                // selecting optimal camera preview size
                {
                    double minDiff = Double.MAX_VALUE;
                    for (Size size : sizes) {
                        if (Math.abs(size.height - height) < minDiff) {
                            mFrameWidth = (int) size.width;
                            mFrameHeight = (int) size.height;
                            minDiff = Math.abs(size.height - height);
                        }
                    }
                }

                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
            }
        }
        
        synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mIntermediateMat = new Mat();
        }
        
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.i(TAG, "surfaceCreated");
        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
        if (mCamera.isOpened()) {
            (new Thread(this)).start();
        } else {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Failed to open native camera");
        }		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.i(TAG, "surfaceDestroyed");
        if (mCamera != null) {
            synchronized (this) {
                mCamera.release();
                mCamera = null;
            }
        }
	}
	
	@Override
	public void run() {
		Log.i(TAG, "Starting processing thread");
		
		Bitmap bmp = null;
		
        while (true) {
            //Bitmap bmp = null;
        	if ((bmp == null) && (0 != mFrameHeight)) {
        		bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
        	}

            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }

                bmp = processFrame(mCamera, bmp);
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                    mHolder.unlockCanvasAndPost(canvas);
                }
                //bmp.recycle();
            }
        }
        
        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();

            mRgba = null;
            mGray = null;
            mIntermediateMat = null;
        }

        Log.i(TAG, "Finishing processing thread");
		
	}
	
	protected Bitmap processFrame(VideoCapture capture, Bitmap bmp) {
		
		capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
		//Threshold(mGray.getNativeObjAddr());
		Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

        if (Utils.matToBitmap(mRgba, bmp))
            return bmp;

        //bmp.recycle();
        return null;
	}
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
	
	public native void Threshold(long matAddrGr);
	
	static {
        System.loadLibrary("camerax");
    }

}
