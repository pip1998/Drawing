package com.dc.drawing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;	

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class DrawingActivity extends Activity {

	DrawingSurfaceView surface;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		FrameLayout surfaceLayout = new FrameLayout(this);		
		surface = new DrawingSurfaceView(this);
		LinearLayout surfaceWidgets = new LinearLayout(this);
				
		Button client = new Button(this);
		client.setText("Set as Client");
		client.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {				
				try
				{
					Intent clientSrv = new Intent(DrawingActivity.this, ClientService.class);
					startService(clientSrv);
				}
				catch (Exception e)
				{
					Log.d("client_exception", e.toString());
				}
			}			
		});
		
		Button server = new Button(this);
		server.setText("Set as Server");
		server.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {				
				try
				{
					Intent serverSrv = new Intent(DrawingActivity.this, ServerService.class);
					startService(serverSrv);
				}
				catch (Exception e)
				{
					Log.d("server_exception", e.toString());
				}
			}			
		});
		
		surfaceWidgets.addView(client);		
		surfaceWidgets.addView(server);
		
		surfaceLayout.addView(surface);
		surfaceLayout.addView(surfaceWidgets);		
				
		setContentView(surfaceLayout);
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	surface.surfaceDestroyed(surface.getHolder());
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	surface.surfaceDestroyed(surface.getHolder());
    }
}