package com.dc.drawing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
	
	public ArrayList<Shape> incomingShapes;
	private ArrayList<Shape> outgoingShapes;
		
	Socket clientSocket = null;
	
    PrintWriter out = null;
    ObjectOutputStream obj_out = null;
    
    BufferedReader in = null;
    ObjectInputStream obj_in = null;    
    
    Calendar calendar = new GregorianCalendar();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		incomingShapes = new ArrayList<Shape>();
		outgoingShapes = new ArrayList<Shape>();
		
		Log.d("Client.onCreate()", "The client service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		clientThread = new Thread(new Runnable() {

			public void run() {
				try {
					Looper.prepare();
					
					while (!stopped) {						
						clientSocket = new Socket("10.0.2.2", 5000);
						//OUTPUT Send the shapes.
						if(!outgoingShapes.isEmpty())
						{		
							OutputStream outStream = clientSocket.getOutputStream();
							obj_out = new ObjectOutputStream(outStream);
							Shape toSend = outgoingShapes.remove(0);
							obj_out.writeObject(toSend);
							obj_out.flush();
						}
						
						new ClientConnectionHandler(ClientService.this, clientSocket);
					}					
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("ClientService", "Error in Listener", e);
				}

				try {
					clientSocket.close();
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
	
	public void addShape(Shape shapeToAdd) {
		this.outgoingShapes.add(shapeToAdd);
	}
}
