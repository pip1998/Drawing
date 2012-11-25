package com.dc.drawing;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;

import com.dc.drawing.ClientService.LocalClientBinder;
import com.dc.drawing.ServerService.LocalServerBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/*
telnet localhost 5554
redir add tcp:5000:6000
*/
/*
telnet localhosts 5556
redir add tcp:6000:5000
*/
public class DrawingActivity extends Activity {

	Timer timer = new Timer();
	
	ServerService mServerService;
	ClientService mClientService;
	boolean mClientBound = true;
	boolean mServerBound = true;
	
	DrawingSurfaceView surface;
	Button setEditing;
	Button next;
	Button prev;
	Button del;
	SeekBar sizeSlider;
	
	ArrayAdapter<String> colorAdapter;
	ArrayList<String> colorArray;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		FrameLayout surfaceLayout = new FrameLayout(this);		
		surface = new DrawingSurfaceView(this);		
		final LinearLayout surfaceWidgets = new LinearLayout(this);		
		
				
		/*
		 * Colour Selection
		 */
		final Spinner colorSpinner = new Spinner(this);	
		colorArray = new ArrayList<String>();
		colorArray.add("Black");
		colorArray.add("Blue");
		colorArray.add("Green");
		colorArray.add("Red");
		colorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, colorArray);
		colorSpinner.setAdapter(colorAdapter);		
		colorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {		    	
		    	switch(position)
		    	{
			    	case 0:
			    		//It's black.
			    		//surface.setColour(0,0,0);		
			    		surface.setColour(Color.BLACK);
			    		break;
			    	case 1:
			    		//It's blue.
			    		//surface.setColour(0,0,255);
			    		surface.setColour(Color.BLUE);
			    		break;
			    	case 2:
			    		//It's green.
			    		//surface.setColour(0,255,0);
			    		surface.setColour(Color.GREEN);
			    		break;
			    	case 3:
			    		//It's red.
			    		//surface.setColour(255,0,0);
			    		surface.setColour(Color.RED);
			    		break;
			    	default:
			    		//surface.setColour(0,0,0);
			    		surface.setColour(Color.BLACK);
		    	}
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        surface.setColour(0,0,0);
		    }
		});
		
		
		
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


		/*
		Button sendShape = new Button(this);
		sendShape.setText("Send Shape");
		sendShape.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {				
				try
				{
					mClientService.AddShapes(surface.getShapes());
				}
				catch (Exception e)
				{
					Log.d("send_shape_exception", e.toString());
				}
			}			
		});*/

		
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
			int oldSliderValue, oldSelectedColour;
			@Override
			public void onClick(View v) {
				
				if (surface.isEditing()) {
					//End editing mode
					setEditing.setText("Edit");
					surface.commitEdits();
					
					//return UI to its previous state
					sizeSlider.setProgress(oldSliderValue);
					colorSpinner.setSelection(oldSelectedColour, true);
					next.setEnabled(false);
					prev.setEnabled(false);
					del.setEnabled(false);
				} else {
					//Begin editing mode
					setEditing.setText("Done");
					surface.setEditing();
					
					//save previous state so we can return to it
					oldSliderValue = sizeSlider.getProgress();
					oldSelectedColour = colorSpinner.getSelectedItemPosition();
					
					//check for button stuff. Changes slider also.
					buttonCheck();
				}
			}			
		});

		surfaceWidgets.addView(client);		
		surfaceWidgets.addView(server);

		//surfaceWidgets.addView(sendShape);		

		surfaceWidgets.addView(colorSpinner);		
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
		
		if (mClientService!=null) {
			mClientService.AddShapeToOutgoingList(s);
		}
		else if (mServerService!=null) {
			mServerService.AddShapeToOutgoingList(s);
		}
		else
		{
			Assert.fail("BREAK, CRITICAL ASTOUNDINGLY BAD ERROR");
		}
	}
	
	private void onClientTimerTick() {
		try {
        	final ArrayList<Shape> shapes = mClientService.GetAndDeleteReceivedShapes();
        	if(!shapes.isEmpty())
        	{
        		runOnUiThread(new Runnable() {
        		    public void run() {
        		    	surface.drawReceivedShape(shapes);
        		    }
        		});	        		
        	}
        } catch (Throwable t) {
            Log.e("onClientTimerTick", "Timer Tick Failed.", t);            
        }
	}
	
	private void onServerTimerTick() {
        try {
        	final ArrayList<Shape> shapes = mServerService.GetAndDeleteReceivedShapes();
        	if(!shapes.isEmpty())
        	{
        		runOnUiThread(new Runnable() {
        		    public void run() {
        		    	surface.drawReceivedShape(shapes);
        		    }
        		});	        		
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