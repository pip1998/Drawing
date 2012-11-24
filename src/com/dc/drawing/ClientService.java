package com.dc.drawing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
	PrintWriter out = null;
	ObjectOutputStream obj_out = null;
	ObjectInputStream obj_in = null;
	BufferedReader in = null;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		outgoingShapes = new ArrayList<Shape>();

		Log.d("Client.onCreate()", "The client service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		clientThread = new Thread(new Runnable() {
			public void run() {
				try {
					Looper.prepare();
					
					while (!stopped) {
						clientSocket = new Socket("10.0.2.2", 5000);
						
						while (!outgoingShapes.isEmpty()) {
							OutputStream outStream = clientSocket.getOutputStream();
							obj_out = new ObjectOutputStream(outStream);
							Shape toSend = outgoingShapes.remove(0);
							obj_out.writeObject(toSend);
							obj_out.flush();							
						}
						
						clientSocket.close();
						
						/*
						ObjectInputStream obj_in = null;
						obj_in = new ObjectInputStream(clientSocket.getInputStream());						
						Shape receivedShape = null;
						while ((receivedShape = (Shape) obj_in.readObject()) != null) {
							try {
								incomingShapes.add(receivedShape);
							} catch (Exception e) {
								Log.e("error", e.toString());
							} finally {
								obj_in.close();
								clientSocket.close();
							}
						}*/
						
						
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
		
		/*
		try
		{
			// Send the shapes.
			obj_in = new ObjectInputStream(inStream);																	
			Shape received;					
			while((received = (Shape) obj_in.readObject()) != null)
			{
				incomingShapes.add(received);
			}
		}
		catch (EOFException eof)
		{
			//ignore end of file exception, just means no data to read for now.
		}*/
	}

	//TODO: Delete this when you push the button remove to production.
	//public void AddShapes(ArrayList<Shape> shapesToAdd) {
	//	this.outgoingShapes.addAll(shapesToAdd);
	//s}

	public void AddShapeToOutgoingList(Shape shapeToAdd) {
		this.outgoingShapes.add(shapeToAdd);
	}
	
	public ArrayList<Shape> GetAndDeleteReceivedShapes() {
		ArrayList<Shape> shapes = new ArrayList<Shape>(this.incomingShapes);
		this.incomingShapes.clear();
		return shapes;
	}
}