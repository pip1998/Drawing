package com.dc.drawing;

import java.io.ObjectOutputStream;
import java.net.Socket;

import android.util.Log;

public class ServerSendHandler implements Runnable {

	// Socket connection to handle.
	private ServerService service;
	private Socket socket;
	private boolean socketOpen = true;
	
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
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());			
			
			//Wait until we have an object to send.
			while(service.outgoingShapes.isEmpty() && socketOpen)
			{
				//Would be nice to sleep, wait calls throw exceptions, though.
				if(socket.isClosed())
				{
					socketOpen = false;
				}
				
				Thread.sleep(100);
			}			
			
			//Only way for that loop to end is the socket closed, or we have an
			// object to add. Do nothing if the socket closed.
			if(socketOpen)
			{
				Shape toSend = service.outgoingShapes.remove(0);
				Log.d("ClientSendingShape", String.valueOf(toSend.getTag()));
				out.writeObject(toSend);
				out.flush();
			}			
		} catch (Exception e ) { e.printStackTrace(); }
	}	
}
