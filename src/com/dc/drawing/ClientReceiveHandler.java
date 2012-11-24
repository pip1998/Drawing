package com.dc.drawing;

import java.io.ObjectInputStream;
import java.net.Socket;

import android.util.Log;

public class ClientReceiveHandler implements Runnable {

	// Socket connection to handle.
	private ClientService service;
	private Socket socket;

	public ClientReceiveHandler(ClientService service, Socket socket) {
		this.socket = socket;
		this.service = service;
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {			 
			ObjectInputStream obj_in = null;
			obj_in = new ObjectInputStream(socket.getInputStream());
			
			Shape receivedShape = null;
			while ((receivedShape = (Shape) obj_in.readObject()) != null) {
				try {
					service.incomingShapes.add(receivedShape);
				} catch (Exception e) {
					Log.e("error", e.toString());
				} finally {
					obj_in.close();
					socket.close();
				}
			}
		} catch (Exception e) {

		}		
	}	
}
