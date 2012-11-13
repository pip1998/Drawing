package com.dc.drawing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class ServerService extends Service {
	
	public class LocalServerBinder extends Binder {
		ServerService getService() {            
            return ServerService.this;
        }
    }

	// Binder given to clients
    private final IBinder mBinder = new LocalServerBinder();

	public ArrayList<Shape> incomingShapes;
	
	private boolean stopped = false;
	private Thread serverThread;
	private ServerSocket ss;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		incomingShapes  = new ArrayList<Shape>();

		Log.d("Server.onCreate()", "The server service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");
		
		serverThread = new Thread(new Runnable() {

			public void run() {
				try {
					Looper.prepare();
					ss = new ServerSocket(6000);
					Log.d("ip: ", ss.getInetAddress().toString()); 	
					//ss.setReuseAddress(true);
					//ss.setPerformancePreferences(100, 100, 1);
					
					while (!stopped) {							
						try {
							//This is blocking.
							Socket connection = ss.accept();
							new ConnectionHandler(ServerService.this, connection);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("Server Service", "Error in Listener", e);
				}

				try {
					ss.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "keep it simple");
				}
			}

		}, "Server thread");
		serverThread.start();

	}

	@Override
	public void onDestroy() {
		stopped = true;
		try {
			ss.close();
		} catch (IOException e) {
		}
		serverThread.interrupt();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
		}
	}
	
	public ArrayList<Shape> GetAndDeleteReceivedShapes()
	{
		ArrayList<Shape> shapes = new ArrayList<Shape>(this.incomingShapes);
		this.incomingShapes.clear();
		return shapes;
	}
}
