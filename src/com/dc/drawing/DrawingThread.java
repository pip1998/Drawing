package com.dc.drawing;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

class DrawingThread extends Thread {
	private SurfaceHolder _surfaceHolder;
	private DrawingSurfaceView drawingSurface;
	private boolean _run = false;

	public DrawingThread(SurfaceHolder surfaceHolder, DrawingSurfaceView view) {
		Log.d("Thread","Spawned the thread");
		_surfaceHolder = surfaceHolder;
		drawingSurface = view;
	}

	public void setRunning(boolean run) {
		_run = run;
	}

	public SurfaceHolder getSurfaceHolder() {
		return _surfaceHolder;
	}

	@Override
	public void run() {
		Canvas c;
		while (_run) {
			c = null;
			try {
				c = _surfaceHolder.lockCanvas(null);
				synchronized (_surfaceHolder) {
					drawingSurface.onDraw(c);
				}
			} finally {
				if (c != null) {
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}