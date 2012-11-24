package com.dc.drawing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
	private ArrayList<Shape> outgoingShapes;
	
	private boolean stopped = false;
	private Thread serverThread;
	private ServerSocket serverSocket;
	
	PrintWriter out = null;
    ObjectOutputStream obj_out = null;
    
    BufferedReader in = null;
    ObjectInputStream obj_in = null;    
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		incomingShapes  = new ArrayList<Shape>();
		outgoingShapes  = new ArrayList<Shape>();

		Log.d("Server.onCreate()", "The server service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");
		
		serverThread = new Thread(new Runnable() {
			public void run() {
				try {
					Looper.prepare();
					serverSocket = new ServerSocket(6000);
					Log.d("ip: ", serverSocket.getInetAddress().toString()); 	
					
					while (!stopped) {							
						try {
							//This is blocking.
							Socket serverSocketConnection = serverSocket.accept();
							
							if(!outgoingShapes.isEmpty())
							{		
								OutputStream outStream = serverSocketConnection.getOutputStream();
								obj_out = new ObjectOutputStream(outStream);
								Shape toSend = outgoingShapes.remove(0);
								obj_out.writeObject(toSend);
								obj_out.flush();
							}
							
							new ServerConnectionHandler(ServerService.this, serverSocketConnection);
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
					serverSocket.close();
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
			serverSocket.close();
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
