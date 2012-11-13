package com.dc.drawing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Looper;
import android.util.Log;

public class ClientService extends Service {

	public class LocalClientBinder extends Binder {
        ClientService getService() {            
            return ClientService.this;
        }
    }
	
	// Binder given to clients
    private final IBinder mBinder = new LocalClientBinder();
	
	private boolean stopped = false;
	private Thread clientThread;
	
	private ArrayList<Shape> outgoingShapes;
		
	Socket echoSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    
    Calendar calendar = new GregorianCalendar();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		outgoingShapes  = new ArrayList<Shape>();
		
		Log.d("Client.onCreate()", "The client service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		clientThread = new Thread(new Runnable() {

			public void run() {
				try {
					Looper.prepare();
					echoSocket = new Socket("10.0.2.2", 5000);
					out = new PrintWriter(echoSocket.getOutputStream(), true);
		            in = new BufferedReader(new InputStreamReader(
		                                        echoSocket.getInputStream()));					
					
					while (!stopped) {						
						Log.d("Client Loop", "Loop.");
						
						//Send the shapes.
						if(!outgoingShapes.isEmpty())
						{
							//
						}
												
						//int second = calendar.get(Calendar.SECOND);
						int seconds = (int)System.currentTimeMillis();
						if(seconds % 4000 == 0)
						{
							String userInput = "This is totally input.";						
							out.println(userInput);
						}
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e(getClass().getSimpleName(), "Error in Listener", e);
				}

				try {
					echoSocket.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "keep it simple");
				}
			}

		}, "Client thread");
		clientThread.start();
	}
	
	public void AddShapes(ArrayList<Shape> shapesToAdd)
	{
		this.outgoingShapes.addAll(shapesToAdd);
	}
}
