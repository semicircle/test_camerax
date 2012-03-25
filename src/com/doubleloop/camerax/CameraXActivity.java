package com.doubleloop.camerax;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class CameraXActivity extends Activity {
	private static final String TAG             = "CameraXActivity";
	
	CameraPreviewSurfaceView mySurface;
	
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
        mySurface = new CameraPreviewSurfaceView(sfh);
        sfh.addCallback(mySurface);
        
        SeekBar seekBar1 = (SeekBar)findViewById(R.id.seek1);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {
				mySurface.mParameter1 = arg0.getProgress();
			}}); 
        
        
        SeekBar seekBar2 = (SeekBar)findViewById(R.id.seek2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {
				mySurface.mParameter2 = arg0.getProgress();
			}}); 
        
        
        SeekBar seekBar3 = (SeekBar)findViewById(R.id.seek3);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {
				mySurface.mParameter3 = arg0.getProgress();
			}});    
        
        Button button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if (mySurface.mMode == 0) {
					mySurface.mMode = CameraPreviewSurfaceView.SUPPORTED_MODE_NUMBER - 1;
				} else {
					mySurface.mMode = (mySurface.mMode - 1) % CameraPreviewSurfaceView.SUPPORTED_MODE_NUMBER;
				}
			}});
        
        Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if (mySurface.mMode ==  CameraPreviewSurfaceView.SUPPORTED_MODE_NUMBER) {
					mySurface.mMode = 0;
				} else {
					mySurface.mMode = (mySurface.mMode + 1) % CameraPreviewSurfaceView.SUPPORTED_MODE_NUMBER;
				}
			}});
        
    }
}