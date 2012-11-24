package com.dc.drawing;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.dc.drawing.ServerService;
import com.dc.drawing.Shape;

import android.util.Log;

public class ServerReceiveHandler implements Runnable {

	// Socket connection to handle.
	private ServerService service;
	private Socket socket;

	public ServerReceiveHandler(ServerService service, Socket socket) {
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