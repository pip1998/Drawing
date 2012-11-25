package com.dc.drawing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

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
	Thread clientThread;

	ArrayList<Shape> outgoingShapes;
	ArrayList<Shape> incomingShapes;

	Socket clientSocket = null;
	ObjectInputStream obj_in = null;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		outgoingShapes = new ArrayList<Shape>();
		incomingShapes = new ArrayList<Shape>();

		Log.d("Client.onCreate()", "The client service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		clientThread = new Thread(new Runnable() {
			public void run() {
				try {
					Looper.prepare();
					
					while (!stopped) {
						clientSocket = new Socket("10.0.2.2", 5000);
						
						new ClientSendHandler(ClientService.this, clientSocket);
						
						ObjectInputStream obj_in = new ObjectInputStream(clientSocket.getInputStream());
						
						Shape receivedShape = null;
						try {
							receivedShape = (Shape) obj_in.readObject();
							incomingShapes.add(receivedShape);
						} catch (Exception e) { e.printStackTrace(); }
						finally {
							obj_in.close();						
							clientSocket.close();
						}
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
	
	public void AddShapeToOutgoingList(Shape shapeToAdd) {
		this.outgoingShapes.add(shapeToAdd);
	}
	
	public ArrayList<Shape> GetAndDeleteReceivedShapes() {
		ArrayList<Shape> shapes = new ArrayList<Shape>(this.incomingShapes);
		this.incomingShapes.clear();
		return shapes;
	}
}