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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

//telnet localhost 5554
//redir add tcp:5000:6000

public class DrawingActivity extends Activity {

	Timer timer = new Timer();
	
	ServerService mServerService;
	ClientService mClientService;
	boolean mBound = true;
	
	DrawingSurfaceView surface;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		FrameLayout surfaceLayout = new FrameLayout(this);		
		surface = new DrawingSurfaceView(this);
		LinearLayout surfaceWidgets = new LinearLayout(this);
				
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
					Shape toSend = new Shape();
					ArrayList<Shape> shapesToSend = new ArrayList<Shape>();
					shapesToSend.add(toSend);
					mClientService.AddShapes(shapesToSend);

				}
				catch (Exception e)
				{
					Log.d("server_exception", e.toString());
				}
			}			
		});
		
		surfaceWidgets.addView(client);		
		surfaceWidgets.addView(server);
		surfaceWidgets.addView(sendShape);
		
		surfaceLayout.addView(surface);
		surfaceLayout.addView(surfaceWidgets);		
				
		setContentView(surfaceLayout);
	}
	
	private void onTimerTick() {
        Log.i("TimerTick", "Timer check for shapes.");
        try {
        	if(mServerService != null)
        	{
	        	ArrayList<Shape> shapes = mServerService.GetAndDeleteReceivedShapes();
	        	if(!shapes.isEmpty())
	        	{
	        		Toast.makeText(getApplicationContext(), "Got a shape.", Toast.LENGTH_LONG).show();
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