package com.dc.drawing;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


//import android.view.SurfaceView;

public class DrawingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private Paint paint = new Paint();
	public DrawingThread _thread;
	private Shape current;
	private ArrayList<Shape> shapes;
	
	private Canvas bCanvas = null;
	private Bitmap bitmap = null;
	private Matrix matrix;
	
	int drawCount = 1;
	
	/*
	 * Constructors
	 */
	public DrawingSurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
		_thread = new DrawingThread(getHolder(), this);
		shapes = new ArrayList<Shape>();
		
		Log.d("Constructor","Hello, DrawingSurfaceView!");
	}

	public DrawingSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
    public void surfaceCreated(SurfaceHolder holder) {
		Log.d("Surface","Telling thread to start");
        _thread.setRunning(true);
        _thread.start();
        
        Log.d("Surface","Creating bitmap");
		bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
		bCanvas = new Canvas();
		bCanvas.setBitmap(bitmap);
		
		matrix = new Matrix();
    }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.d("Destroyed", "We were interruped?: " + e);
            }
        }
    }

	@Override
	protected void onDraw(Canvas canvas) {
		
		for (Shape s : shapes) {
			for (Path path : ((Line)s).getGraphicsPath()) {
				paint.setAntiAlias(true);
				paint.setDither(true);
		    	paint.setStyle(Paint.Style.STROKE);
		    	paint.setStrokeJoin(Paint.Join.ROUND);
		    	paint.setStrokeCap(Paint.Cap.ROUND);
				paint.setColor(Color.rgb(s.getrgb()[0], s.getrgb()[1], s.getrgb()[2]));
				paint.setStrokeWidth(s.getStrokeWidth());
        	    
				canvas.drawPath(path, paint);
    		}
		}		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (_thread.getSurfaceHolder()) {
			Log.d("Touch","You touched me");
			float touched_x = event.getX();
			float touched_y = event.getY();

			int action = event.getAction();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				current = new Line();
				current.setrgb(255, 0, 255);
				current.setStrokeWidth(5);
				((Line) current).getPath().moveTo(touched_x, touched_y);
				((Line) current).getPath().lineTo(touched_x, touched_y);
				break;
			case MotionEvent.ACTION_MOVE:
				((Line) current).getPath().lineTo(touched_x, touched_y);
				break;
			case MotionEvent.ACTION_UP:
				((Line) current).getPath().lineTo(touched_x, touched_y);
				((Line) current).getGraphicsPath().add(
				((Line) current).getPath());
				shapes.add(current);
				break;
			case MotionEvent.ACTION_CANCEL:

				break;
			case MotionEvent.ACTION_OUTSIDE:

				break;
			default:

			}
			
			return true;
		}
	}
}