package com.dc.drawing;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class DrawingSurfaceView extends View {

  private DrawingActivity parent;
  
  private Paint paint = new Paint();
  private Shape line;
  private SerializablePath path = new SerializablePath();
  
  private int currentWidth = 1;
  private float HALF_STROKE_WIDTH = currentWidth / 2;
  private int currentRed = 0;
  private int currentGreen = 0;
  private int currentBlue = 0;
  
  private ArrayList<Shape> lines = new ArrayList<Shape>();

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
    paint.setAntiAlias(true);
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
	  for (Shape s : lines) {
		  SerializablePath p = s.getPath();
		  paint.setColor(Color.rgb(s.getrgb()[0], s.getrgb()[1], s.getrgb()[2]));
		  paint.setStrokeWidth(s.getStrokeWidth());
		  canvas.drawPath(p,paint);
	  }
	  Log.d("onDraw","Drew " + lines.size() + " lines");
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float eventX = event.getX();
    float eventY = event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
    	line = new Shape(currentWidth, currentRed, currentGreen, currentBlue);
    	path = new SerializablePath();
    	line.setPath(path);
    	lines.add(line);
    	Log.d("Touch","There are now " + lines.size() + " lines in the array");
    	sendDrawnShape(line);
        path.moveTo(eventX, eventY);
        lastTouchX = eventX;
        lastTouchY = eventY;
        // There is no end point yet, so don't waste cycles invalidating.
        return true;

      case MotionEvent.ACTION_MOVE:
      case MotionEvent.ACTION_UP:
        // Start tracking the dirty region.
        resetDirtyRect(eventX, eventY);

        // When the hardware tracks events faster than they are delivered, the
        // event will contain a history of those skipped points.
        int historySize = event.getHistorySize();
        for (int i = 0; i < historySize; i++) {
          float historicalX = event.getHistoricalX(i);
          float historicalY = event.getHistoricalY(i);
          expandDirtyRect(historicalX, historicalY);
          path.lineTo(historicalX, historicalY);
          sendDrawnShape(line);
        }

        // After replaying history, connect the line to the touch point.
        path.lineTo(eventX, eventY);
        sendDrawnShape(line);
        break;

      default:
        return false;
    }

    // Include half the stroke width to avoid clipping.
    invalidate(
        (int) (dirtyRect.left - HALF_STROKE_WIDTH),
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
	  for (Shape s : shapes) {
		  lines.add(s);
		  invalidate();
	  }
  }
  
  public void setLineWidth(int width) {
	  currentWidth = width;
	  HALF_STROKE_WIDTH = currentWidth / 2;
  }
  
  private void sendDrawnShape(Shape s) {
	  parent.sendShapeFromDrawingSurface(s);
  }
}