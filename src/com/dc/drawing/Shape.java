package com.dc.drawing;

import java.io.Serializable;

import android.graphics.Path;

public class Shape extends Object implements Serializable  
{
	private static final long serialVersionUID = -685099400005109050L;
			private int[] shapeColour;
			protected SerializablePath shapePath;
			protected float strokeWidth;
	    	
			public Shape(){
	    		shapePath = new SerializablePath();
	    		shapeColour = new int[3];
	    		shapeColour[0] = 0;
	    		shapeColour[1] = 0;
	    		shapeColour[2] = 0;
	    		strokeWidth = 3;
	    	}
	    	
	    	
	    	public Path getPath(){
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
	    	
	    	public void setStrokeWidth(float w){
	    		strokeWidth = w;
	    	}
	    	
	    	public float getStrokeWidth(){
	    		return strokeWidth;
	    	}
}