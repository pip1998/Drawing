package com.dc.drawing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class ClientService extends Service {

	private boolean stopped = false;
	private Thread clientThread;
		
	Socket echoSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("Client.onCreate()", "The client service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		clientThread = new Thread(new Runnable() {

			public void run() {
				try {
					Looper.prepare();
					echoSocket = new Socket("localhost", 12345);
					out = new PrintWriter(echoSocket.getOutputStream(), true);
		            in = new BufferedReader(new InputStreamReader(
		                                        echoSocket.getInputStream()));					
					
					while (!stopped) {						
						Log.d("Client Loop", "Loop.");
						
						String userInput = "This is totally input.";						
						out.println(userInput);
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

		}, "Server thread");
		clientThread.start();
	}
}
