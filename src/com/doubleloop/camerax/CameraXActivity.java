package com.doubleloop.camerax;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class CameraXActivity extends Activity {
	private static final String TAG             = "CameraXActivity";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        SurfaceView sfv;  
        SurfaceHolder sfh;  
        sfv = (SurfaceView) this.findViewById(R.id.cameraPreviewSurfaceView1);  
        sfh = sfv.getHolder();  
        sfh.addCallback(new CameraPreviewSurfaceView(sfh));
    }
}