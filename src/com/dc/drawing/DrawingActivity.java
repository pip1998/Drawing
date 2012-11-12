package com.dc.drawing;

import android.app.Activity;
import android.os.Bundle;

public class DrawingActivity extends Activity {

	DrawingSurfaceView surface;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		surface = new DrawingSurfaceView(this);
		//surface.setBackgroundColor(Color.WHITE);
		setContentView(surface);
	}
}