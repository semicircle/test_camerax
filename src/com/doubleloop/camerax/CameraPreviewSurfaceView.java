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
	private static final String TAG = "CameraPreviewSurfaceView";

    private SurfaceHolder       mHolder;
    private Bitmap mBitmap = null;
    /* used when drawing the canvas */
    private int mSurfLeftSpace = 0;
    private int mSurfTopSpace = 0;
    private int mMode = 0;
    private VideoCapture        mCamera;
    
    //private Mat mRgba;
    //private Mat mGray;
    //private Mat mIntermediateMat;
    //int mFrameWidth;
    //int mFrameHeight;
    
    //private long mGraySubmatNativeAddr;
    //private long mRgbaNativeAddr;

	public CameraPreviewSurfaceView(SurfaceHolder holder) {
		mHolder = holder;
        //mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
        //mFrameWidth = 0;
        //mFrameHeight = 0;
	}

	

	@Override
	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceCreated");
		int fixWidth = 0;
		int fixHeight = 0;
		int ret;

        if (mCamera != null && mCamera.isOpened()) {
            Log.i(TAG, "before mCamera.getSupportedPreviewSizes()");
            List<Size> sizes = mCamera.getSupportedPreviewSizes();
            Log.i(TAG, "after mCamera.getSupportedPreviewSizes()");
            fixWidth = width;
            fixHeight = height;
            // selecting optimal camera preview size
            {
                double minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                    	fixWidth = (int) size.width;
                    	fixHeight = (int) size.height;
                        minDiff = Math.abs(size.height - height);
                    }
                }
            }

            mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, fixWidth);
            mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, fixHeight);
            
            mCamera.release();
            mCamera = null;
            
            ret = NativeInit(mCamera, fixWidth, fixHeight);
    		
    		mSurfLeftSpace = (width - fixWidth) / 2;
    		mSurfTopSpace = (height - fixHeight) / 2;
    		mBitmap = Bitmap.createBitmap(fixWidth, fixHeight, Bitmap.Config.ARGB_8888);
    			
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
//        if (mCamera != null) {
//            synchronized (this) {
//                mCamera.release();
//                mCamera = null;
//            }
//        }
		NativeDestory();
	}
	
	@Override
	public void run() {
		Log.i(TAG, "Starting processing thread");
		
		
		
        while (true) {
            
//        	if ((bmp == null) && (0 != mFrameHeight)) {
        		//bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
//        	}

//            synchronized (this) {
//                if (mCamera == null)
//                    break;
//
//                if (!mCamera.grab()) {
//                    Log.e(TAG, "mCamera.grab() failed");
//                    break;
//                }
//
//                bmp = processFrame(mCamera, bmp);
//            }
        	if (mBitmap != null) {
	        	NativeProcessFrame(mBitmap, mMode);
	
	            
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(mBitmap, mSurfTopSpace, mSurfLeftSpace, null);
                    mHolder.unlockCanvasAndPost(canvas);
                }
                
	            
        	}
        }
        
//        synchronized (this) {
//            // Explicitly deallocate Mats
//            if (mRgba != null)
//                mRgba.release();
//            if (mGray != null)
//                mGray.release();
//            if (mIntermediateMat != null)
//                mIntermediateMat.release();
//
//            mRgba = null;
//            mGray = null;
//            mIntermediateMat = null;
//        }

//        Log.i(TAG, "Finishing processing thread");
		
	}
	
//	protected Bitmap processFrame(VideoCapture capture, Bitmap bmp) {
//		
//		/*
//		capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
//		
//		Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
//        FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
//
//        if (Utils.matToBitmap(mRgba, bmp))
//            return bmp;
//		*/
//        //bmp.recycle();
//        return null;
//	}
	
//	public native void FindFeatures(long matAddrGr, long matAddrRgba);
	
	//public native void Threshold(long matAddrGr);
	
	/* design:
	 * initialize: 
	 * NativeInit for native code to initial Mats , camera and calculate the most fit size.
	 * NativeInit_GetBmpInfo, for native to get bitmap info for one time, instead of doing this every frame.
	 * NativeProcessFrame process
	 * process:
	 * NativeProcessFrame, do frame capture, and returns bitmap 
	 * */
	
	public native int NativeInit(VideoCapture camera, int width, int height);
	public native void NativeProcessFrame(Bitmap bmp, int mode);
	public native void NativeDestory();
	
	static {
        System.loadLibrary("camerax");
    }

}
