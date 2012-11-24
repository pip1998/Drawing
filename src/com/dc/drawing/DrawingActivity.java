package com.dc.drawing;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
public class DrawingActivity extends Activity {

	Timer timer = new Timer();
	
	ServerService mServerService;
	ClientService mClientService;
	boolean mBound = true;
	
	DrawingSurfaceView surface;
	
	Spinner colorSpinner;
	ArrayAdapter<String> colorAdapter;
	ArrayList<String> colorArray;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		FrameLayout surfaceLayout = new FrameLayout(this);		
		surface = new DrawingSurfaceView(this);		
		LinearLayout surfaceWidgets = new LinearLayout(this);		
		
		colorSpinner = new Spinner(this);	
		colorArray = new ArrayList<String>();
		colorArray.add("Black");
		colorArray.add("Blue");
		colorArray.add("Green");
		colorArray.add("Red");
		colorAdapter = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_spinner_dropdown_item, 
				colorArray);
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
		
		final Button client = new Button(this);
		final Button server = new Button(this);
		final SeekBar sizeSlider = new SeekBar(this);
		
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
					
					//ADD SOME SHAPES LIKE THIS. NOT HERE THOUGH.
					//mClientService.AddShapes(shapesToAdd);
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
					timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 0, 100L);
				}
				catch (Exception e)
				{
					Log.d("server_exception", e.toString());
				}
			}			
		});

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
					Log.d("server_exception", e.toString());
				}
			}			
		});
		
		/*
		 * SeekBar for adjusting new line sizes
		 */
		
		sizeSlider.setMax(50);
        sizeSlider.setProgress(1);
		LayoutParams lp = new LayoutParams(200, 30);
        sizeSlider.setLayoutParams(lp);
		sizeSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d("Activity","Setting line width to " + progress);
				surface.setLineWidth(progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		surfaceWidgets.addView(client);		
		surfaceWidgets.addView(server);
		surfaceWidgets.addView(sendShape);		
		surfaceWidgets.addView(colorSpinner);		
		surfaceWidgets.addView(sizeSlider);
		
		surfaceLayout.addView(surface);
		surfaceLayout.addView(surfaceWidgets);		
				
		setContentView(surfaceLayout);
		surface.setParent(this);
	}
	
	public void sendShapeFromDrawingSurface(Shape s) {
		if (mClientService!=null) {
			mClientService.addShape(s);
		}
	}
	
	private void onTimerTick() {
//        Log.i("TimerTick", "Timer check for shapes.");
        try {
        	if(mServerService != null)
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
        } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
            Log.e("TimerTick", "Timer Tick Failed.", t);            
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
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
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
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}