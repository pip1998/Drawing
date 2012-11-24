package com.dc.drawing;

import java.io.Serializable;

public class Shape extends Object implements Serializable  
{
	private static final long serialVersionUID = -685099400005109050L;
			private int[] shapeColour;
			protected SerializablePath shapePath;
			protected float strokeWidth;
			private long tag;
	    	
			public Shape(int strokeWidth, int r, int g, int b){
	    		shapePath = new SerializablePath();
	    		shapeColour = new int[3];
	    		shapeColour[0] = r;
	    		shapeColour[1] = g;
	    		shapeColour[2] = b;
	    		this.strokeWidth = (float)(strokeWidth);
	    		tag = System.currentTimeMillis();
	    	}
	    		    	
	    	public SerializablePath getPath(){
	    		return shapePath;
	    	}
	    	
	    	public void setPath(SerializablePath p){
	    		shapePath = p;
	    	}
	    	
	    	public void setrgb(int red, int green, int blue){
	    		shapeColour[0] = red;
	    		shapeColour[1] = green;
	    		shapeColour[2] = blue;
	    	}
	    	
	    	public int[] getrgb(){
	    		return shapeColour;
	    	}
	    	
	    	public void setStrokeWidth(int w){
	    		strokeWidth = w;
	    	}
	    	
	    	public float getStrokeWidth(){
	    		return strokeWidth;
	    	}
	    	
	    	public long getTag() {
	    		return tag;
	    	}
}