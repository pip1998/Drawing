package com.dc.drawing;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingSurfaceView extends View {

	private DrawingActivity parent;
	private Paint paint = new Paint();
	private Shape line;
	private SerializablePath path = new SerializablePath();
	private int selectedLineIndex = -1;
	private int currentWidth = 1;
	private float HALF_STROKE_WIDTH = currentWidth / 2;
	private int currentRed = 0;
	private int currentGreen = 0;
	private int currentBlue = 0;

	PathEffect editingEffect = new DashPathEffect(new float[] { 10, 20 }, 0);
	PathEffect normalEffect = null;

	private ArrayList<Shape> lines = new ArrayList<Shape>();

	private boolean editing = false;

	/**
	 * Optimizes painting by invalidating the smallest possible area.
	 */
	private float lastTouchX;
	private float lastTouchY;
	private final RectF dirtyRect = new RectF();

	public DrawingSurfaceView(Context context) {
		super(context);

		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);

	}

	public void setParent(DrawingActivity parent) {
		this.parent = parent;
	}

	public void clear() {
		path.reset();

		// Repaints the entire view.
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		for (int x = 0; x < lines.size(); x++) {
			Shape s = lines.get(x);
			SerializablePath p = s.getPath();
			paint.setColor(Color.rgb(s.getrgb()[0], s.getrgb()[1],s.getrgb()[2]));
			paint.setStrokeWidth(s.getStrokeWidth());
			if (x == selectedLineIndex) {
				paint.setPathEffect(editingEffect);
			} else {
				paint.setPathEffect(null);
			}
			canvas.drawPath(p, paint);
		}

		Log.d("onDraw", "Drew " + lines.size() + " lines");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (editing) {
			return true;
		}
		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			line = new Shape(currentWidth, currentRed, currentGreen,
					currentBlue);
			path = new SerializablePath();
			line.setPath(path);
			lines.add(line);
			Log.d("Touch", "Created a new line (Tag: " + line.getTag());
			path.moveTo(eventX, eventY);
			lastTouchX = eventX;
			lastTouchY = eventY;
			// There is no end point yet, so don't waste cycles invalidating.
			return true;

		case MotionEvent.ACTION_MOVE:
			// Start tracking the dirty region.
			resetDirtyRect(eventX, eventY);

			// When the hardware tracks events faster than they are delivered,
			// the
			// event will contain a history of those skipped points.
			int historySize = event.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				float historicalX = event.getHistoricalX(i);
				float historicalY = event.getHistoricalY(i);
				expandDirtyRect(historicalX, historicalY);
				path.lineTo(historicalX, historicalY);
			}

			// After replaying history, connect the line to the touch point.
			path.lineTo(eventX, eventY);
			break;
		case MotionEvent.ACTION_UP:
			sendDrawnShape(line);
			break;

		default:
			return false;
		}

		// Include half the stroke width to avoid clipping.
		invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
				(int) (dirtyRect.top - HALF_STROKE_WIDTH),
				(int) (dirtyRect.right + HALF_STROKE_WIDTH),
				(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

		lastTouchX = eventX;
		lastTouchY = eventY;

		return true;
	}

	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * points.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < dirtyRect.left) {
			dirtyRect.left = historicalX;
		} else if (historicalX > dirtyRect.right) {
			dirtyRect.right = historicalX;
		}
		if (historicalY < dirtyRect.top) {
			dirtyRect.top = historicalY;
		} else if (historicalY > dirtyRect.bottom) {
			dirtyRect.bottom = historicalY;
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 */
	private void resetDirtyRect(float eventX, float eventY) {

		// The lastTouchX and lastTouchY were set when the ACTION_DOWN
		// motion event occurred.
		dirtyRect.left = Math.min(lastTouchX, eventX);
		dirtyRect.right = Math.max(lastTouchX, eventX);
		dirtyRect.top = Math.min(lastTouchY, eventY);
		dirtyRect.bottom = Math.max(lastTouchY, eventY);
	}

	public ArrayList<Shape> getShapes() {
		return lines;
	}

	public void drawReceivedShape(ArrayList<Shape> shapes) {
		for (Shape n : shapes) {
			for (Shape o : lines) {
				if (n.getTag()==o.getTag()) {
					lines.remove(o);
				}
			}
			lines.add(n);
			invalidate();
		}
	}

	public void setLineWidth(int _width) {
		int width = _width+1;
		if (editing) {
			int dashamount = width;
			lines.get(lines.size() - 1).setStrokeWidth(width);
			
			if (dashamount<10) {
				dashamount=10;
			}
			
			editingEffect = new DashPathEffect(new float[] { dashamount, dashamount+10 }, 0);
			
			invalidate();
		} else {
			currentWidth = width;
			HALF_STROKE_WIDTH = currentWidth / 2;
		}
	}

	private void sendDrawnShape(Shape s) {
		parent.sendShapeFromDrawingSurface(s);
	}

	public void setColour(int color) {
		if (editing) {
			lines.get(lines.size() - 1).setrgb(Color.red(color),Color.green(color), Color.blue(color));
			invalidate();
		} else {
			currentRed = Color.red(color);
			currentGreen = Color.green(color);
			currentBlue = Color.blue(color);
		}
	}

	public void setColour(int r, int g, int b) {
		if (editing) {
			lines.get(lines.size() - 1).setrgb(r, g, b);
			invalidate();
		} else {
			currentRed = r;
			currentGreen = g;
			currentBlue = b;
		}
	}

	public void setEditing() {
		Log.d("DEBUG", "Editing is now " + editing);
		editing = true;
		selectedLineIndex = lines.size() - 1; //testing for now
		invalidate();
	}
	
	public void commitEdits() {
		
		//do stuff to send updated shape to friends		
		
		
		selectedLineIndex = -1;
		editing = false;
		invalidate();
//		collisionTest();
	}

	public boolean isEditing() {
		return this.editing;
	}
	
	public void collisionTest() {
		ArrayList<Shape> testshapes = new ArrayList<Shape>();
		int collisions=0;
		long currentTicks = System.currentTimeMillis();
		
		for (int x=0;x<5000;x++) {
			if (currentTicks==System.currentTimeMillis()) {
				//don't do anything
			} else {
				Shape n = new Shape(1,0,0,0);
				for (Shape e : testshapes) {
					if (e.getTag()==n.getTag()) {
						collisions++;
					}
				}
			}
			currentTicks = System.currentTimeMillis();
		}
		Log.d("Collisions","There were " + collisions + "collisions");
	}
	
}