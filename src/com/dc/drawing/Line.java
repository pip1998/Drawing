package com.dc.drawing;

import java.util.ArrayList;

import android.graphics.Path;


public class Line extends Shape {
	private ArrayList<Path>_graphics;
	
	public Line(){
		super();
		 _graphics = new ArrayList<Path>();
	}
	
	public ArrayList<Path> getGraphicsPath(){
		return _graphics;
	}

}