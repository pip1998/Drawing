package com.dc.drawing;

import java.io.Serializable;
import android.graphics.RectF;

public class Shape extends Object implements Serializable  
{
	private static final long serialVersionUID = -685099400005109050L;
			private int[] shapeColour;
			protected SerializablePath shapePath;
			protected float strokeWidth;
			private long tag;
			private boolean delete;
			float[] bounds;
			float totalOffsetX, totalOffsetY;
			
			public Shape(float strokeWidth, int alpha, int r, int g, int b){
	    		shapePath = new SerializablePath();
	    		shapeColour = new int[4];
	    		shapeColour[0] = alpha;
	    		shapeColour[1] = r;
	    		shapeColour[2] = g;
	    		shapeColour[3] = b;
	    		this.strokeWidth = strokeWidth;
	    		tag = System.currentTimeMillis();
	    		delete = false;
	    		bounds = new float[4];
	    		totalOffsetX = 0;
	    		totalOffsetY = 0;
	    	}
	    		    	
	    	public SerializablePath getPath(){
	    		return shapePath;
	    	}
	    	
	    	public void setPath(SerializablePath p){
	    		shapePath = p;
	    	}
	    	
	    	public void setargb(int alpha, int red, int green, int blue){
	    		shapeColour[0] = alpha;
	    		shapeColour[1] = red;
	    		shapeColour[2] = green;
	    		shapeColour[3] = blue;
	    	}
	    	
	    	public int[] getargb(){
	    		return shapeColour;
	    	}
	    	
	    	public void setStrokeWidth(int w) {
	    		strokeWidth = w;
	    	}
	    	
	    	public void setStrokeWidth(float w) {
	    		strokeWidth = w;
	    	}
	    	
	    	public float getStrokeWidth(){
	    		return strokeWidth;
	    	}
	    	
	    	public long getTag() {
	    		return tag;
	    	}
	    	
	    	public boolean deleteOnNextCycle() {
	    		return delete;
	    	}
	    	
	    	public void setDeleteOnNextCycle(boolean b) {
	    		delete = b;
	    	}
	    	
	    	public void setCentre() {
	    		RectF b = new RectF();
	    		shapePath.computeBounds(b, true);
	    		bounds[0]=b.left;
	    		bounds[1]=b.top;
	    		bounds[2]=b.right;
	    		bounds[3]=b.bottom;
	    	}
	    	
	    	public void setOffset(float x, float y) {
	    		totalOffsetX += x;
	    		totalOffsetY += y;
	    	}
	    		    	
	    	public float getTotalOffsetX() {
	    		return totalOffsetX;
	    	}
	    	
	    	public float getTotalOffsetY() {
	    		return totalOffsetY;
	    	}
	    	
	    	public RectF getBounds() {
	    		return new RectF(bounds[0],bounds[1],bounds[2],bounds[3]);
	    	}
}