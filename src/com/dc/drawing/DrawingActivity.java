package com.dc.drawing;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.dc.drawing.ClientService.LocalClientBinder;
import com.dc.drawing.ServerService.LocalServerBinder;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/*
telnet localhost 5554
redir add tcp:5000:6000
*/
@TargetApi(11)
public class DrawingActivity extends Activity {

	Timer timer = new Timer();
	
	ServerService mServerService;
	ClientService mClientService;
	boolean mClientBound = true;
	boolean mServerBound = true;
	
	Button colorDisplayer;
	
	DrawingSurfaceView surface;
	Button setEditing;
	Button next;
	Button prev;
	Button del;
	SeekBar sizeSlider;
	
	ArrayAdapter<String> colorAdapter;
	ArrayList<String> colorArray;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		FrameLayout surfaceLayout = new FrameLayout(this);		
		surface = new DrawingSurfaceView(this);		
		final LinearLayout surfaceWidgets = new LinearLayout(this);		
		
		/*
		 * Client/Server mode buttons
		 */
		final Button client = new Button(this);
		final Button server = new Button(this);
		client.setText("Join Game");
		client.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {				
				try
				{
					Intent clientSrv = new Intent(DrawingActivity.this, ClientService.class);
					bindService(clientSrv, mClientConnection, Context.BIND_AUTO_CREATE);
					startService(clientSrv);
					
					server.setEnabled(false);
					
					timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onClientTimerTick();}}, 0, 100L);
					
				}
				catch (Exception e)
				{
					Log.d("client_exception", e.toString());
				}
			}			
		});
		
		server.setText("Host Game");
		server.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {				
				try
				{
					Intent serverSrv = new Intent(DrawingActivity.this, ServerService.class);
					bindService(serverSrv, mServerConnection, Context.BIND_AUTO_CREATE);
					startService(serverSrv);
					
					client.setEnabled(false);
					
					//Here's how to get shapes. Dont do this here.
					//mServerService.GetAndDeleteReceivedShapes();
					
					//Timer for checking for shapes. Don't start checking until the server service is running.		
					timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onServerTimerTick();}}, 0, 100L);
				}
				catch (Exception e)
				{
					Log.d("server_exception", e.toString());
				}
			}			
		});
	
		final Button colorPicker = new Button(this);
		colorPicker.setText("Pick Brush Color");
		colorPicker.setOnClickListener(new OnClickListener() {
			 @Override
	            public void onClick(View v) {
	                colorpicker();
	            }
		});
		
		//ColorFilter curColor = new LightingColorFilter(surface.getColour(), surface.getColour());
		//ColorFilter curColor = new LightingColorFilter(Color.BLACK, Color.BLACK);
		
		colorDisplayer = new Button(this);
		colorDisplayer.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_OVER);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(38, 38);
		layoutParams.setMargins(2, 2, 2, 4);
		colorDisplayer.setLayoutParams(layoutParams);		

		/*
		 * SeekBar for adjusting new line sizes
		 */
		sizeSlider = new SeekBar(this);
		sizeSlider.setMax(51);
        sizeSlider.setProgress(4);
        surface.setLineWidth(4);
		LayoutParams lp = new LayoutParams(200, 30);
        sizeSlider.setLayoutParams(lp);
		sizeSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d("Activity","Setting line width to " + progress);
				surface.setLineWidth(progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		
		/*Edit Buttons*/
		next = new Button(this);
		prev = new Button(this);
		del  = new Button(this);
		next.setEnabled(false);
		prev.setEnabled(false);
		del.setEnabled(false);
		
		next.setText("Next");
		prev.setText("Prev");
		del.setText("Delete");
		
		next.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				surface.selectNext();
				buttonCheck();
			}
		});
		
		prev.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				surface.selectPrev();
				buttonCheck();
			}
		});
		
		del.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				surface.deleteCurrentItem();
				buttonCheck();				
			}
		});
		
		
		setEditing = new Button(this);
		setEditing.setEnabled(false); //there are no lines to edit initially.
		setEditing.setText("Edit");
		setEditing.setOnClickListener(new OnClickListener()
		{
			int oldSliderValue;
			@Override
			public void onClick(View v) {
				
				if (surface.isEditing()) {
					//End editing mode
					setEditing.setText("Edit");
					surface.commitEdits();
					
					//return UI to its previous state
					sizeSlider.setProgress(oldSliderValue);
					//colorSpinner.setSelection(oldSelectedColour, true);
					next.setEnabled(false);
					prev.setEnabled(false);
					del.setEnabled(false);
				} else {
					//Begin editing mode
					setEditing.setText("Done");
					surface.setEditing();
					
					//save previous state so we can return to it
					oldSliderValue = sizeSlider.getProgress();
					
					//check for button stuff. Changes slider also.
					buttonCheck();
				}
			}			
		});

		surfaceWidgets.addView(client);		
		surfaceWidgets.addView(server);		
		surfaceWidgets.addView(colorPicker);
		surfaceWidgets.addView(colorDisplayer);
		surfaceWidgets.addView(sizeSlider);
		surfaceWidgets.addView(setEditing);
		surfaceWidgets.addView(next);
		surfaceWidgets.addView(prev);
		surfaceWidgets.addView(del);
		surfaceLayout.addView(surface);
		surfaceLayout.addView(surfaceWidgets);		
				
		setContentView(surfaceLayout);
		surface.setParent(this);
	}
	
	public void colorpicker() {
        //     initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
        //     for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
 
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, surface.getColour(), new OnAmbilWarnaListener() {
 
            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog){
            }
 
            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
            	surface.setColour(color);
            	colorDisplayer.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
            }
        });
        dialog.show();
    }
	
	//Check if edit/prev/next/delete buttons should be enabled or not.
	public void buttonCheck() {
		if (!surface.hasItems()) {
			del.setEnabled(false);
			next.setEnabled(false);
			prev.setEnabled(false);
			setEditing.setEnabled(false);
			setEditing.setText("Edit");
			return;
		}
		
		int selectedWidth = (int)(surface.getSelectedShapeWidth());
		if (selectedWidth>=0) {
			sizeSlider.setProgress(selectedWidth);
		}
		
		del.setEnabled(true);
				
		if (surface.isAtLastItem()) {
			next.setEnabled(false);
		} else {
			next.setEnabled(true);
		}
		
		if (surface.isAtFirstItem()) {
			prev.setEnabled(false);
		} else {
			prev.setEnabled(true);
		}
	}
	
	public void sendShapeFromDrawingSurface(Shape s) {
		setEditing.setEnabled(true);
		
		if (mClientService != null) {
			mClientService.AddShapeToOutgoingList(s);
		}
		else if (mServerService != null) {
			mServerService.AddShapeToOutgoingList(s);
		}
	}
	
	private void onClientTimerTick() {
		try {
			if(mClientBound && mClientService != null)
			{
	        	final ArrayList<Shape> shapes = mClientService.GetAndDeleteReceivedShapes();
	        	if(!shapes.isEmpty())
	        	{
	        		runOnUiThread(new Runnable() {
	        		    public void run() {
	        		    	surface.drawReceivedShape(shapes);
	        		    }
	        		});	        		
	        	}
			}
        } catch (Throwable t) {
            Log.e("onClientTimerTick", "Timer Tick Failed.", t);            
        }
	}
	
	private void onServerTimerTick() {
        try {
        	//Sometimes takes a while to finish binding the service, even though its marked bound already.
        	if(mServerBound && mServerService != null)
        	{
	        	final ArrayList<Shape> shapes = mServerService.GetAndDeleteReceivedShapes();
	        	if(!shapes.isEmpty())
	        	{
	        		runOnUiThread(new Runnable() {
	        		    public void run() {
	        		    	surface.drawReceivedShape(shapes);
	        		    }
	        		});	        		
        	}
        	}
        } catch (Throwable t) {
            Log.e("onServerTimerTick", "Timer Tick Failed.", t);            
        }
    }
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mClientConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalClientBinder binder = (LocalClientBinder) service;
            mClientService = binder.getService();
            mClientBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mClientBound = false;
        }
    };
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalServerBinder binder = (LocalServerBinder) service;
            mServerService = binder.getService();
            mServerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServerBound = false;
        }
    };
    
    
}