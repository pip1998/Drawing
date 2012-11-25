package com.dc.drawing;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

public class ServerSendHandler implements Runnable {

	// Socket connection to handle.
	private ServerService service;
	private Socket socket;

	private static ObjectOutputStream out;
	
	public ServerSendHandler(ServerService service, Socket socket) {
		this.socket = socket;
		this.service = service;
		Thread t = new Thread(this, "ServerSendHandler");
		t.start();
	}

	@Override
	public void run() {
		try
		{
			OutputStream outStream = socket.getOutputStream();
			out = new ObjectOutputStream(outStream);			
			
			//Wait until we have an object to send.
			while(service.outgoingShapes.isEmpty())
			{
				//Would be nice to sleep, wait calls throw exceptions, though.			
			}			
									
			Shape toSend = service.outgoingShapes.remove(0);
			Log.d("ClientSendingShape", String.valueOf(toSend.getTag()));
			out.writeObject(toSend);
			out.flush();
			
		} catch (Exception e ) { e.printStackTrace(); }
	}	
}
