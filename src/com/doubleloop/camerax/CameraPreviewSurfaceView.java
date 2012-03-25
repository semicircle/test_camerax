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
    private VideoCapture        mCamera;
    
    /* properties */
    public int mParameter1 = 50;
    public int mParameter2 = 50;
    public int mParameter3 = 50;
    public int mMode = 0;
    
    public static int SUPPORTED_MODE_NUMBER = 2;


	public CameraPreviewSurfaceView(SurfaceHolder holder) {
		mHolder = holder;
        Log.i(TAG, "Instantiated new " + this.getClass());
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
		NativeDestory();
	}
	
	@Override
	public void run() {
		Log.i(TAG, "Starting processing thread");
        while (true) {
            if (mBitmap != null) {
	        	NativeProcessFrame(mBitmap, mMode, mParameter1, mParameter2, mParameter3);
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(mBitmap, mSurfTopSpace, mSurfLeftSpace, null);
                    mHolder.unlockCanvasAndPost(canvas);
                }
        	}
        }
	}
	
	public native int NativeInit(VideoCapture camera, int width, int height);
	public native void NativeProcessFrame(Bitmap bmp, int mode, int mParameter1, int mParameter2, int mParameter3);
	public native void NativeDestory();
	
	static {
        System.loadLibrary("camerax");
    }

}
