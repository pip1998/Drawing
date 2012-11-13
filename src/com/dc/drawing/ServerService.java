package com.dc.drawing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class ServerService extends Service {
	
	public class LocalServerBinder extends Binder {
		ServerService getService() {            
            return ServerService.this;
        }
    }

	// Binder given to clients
    private final IBinder mBinder = new LocalServerBinder();
	
	private Handler handler = new Handler();
	
	private ArrayList<Shape> incomingShapes;
	
	private boolean stopped = false;
	private Thread serverThread;
	private ServerSocket ss;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
					ss.setReuseAddress(true);
					ss.setPerformancePreferences(100, 100, 1);
					while (!stopped) {
						//This is blocking.
						Socket accept = ss.accept();
						accept.setPerformancePreferences(10, 100, 1);
						accept.setKeepAlive(true);
				
						ObjectInputStream obj_in = null;
						try {
							obj_in = new ObjectInputStream(accept.getInputStream());
							Shape receivedShape = (Shape) obj_in.readObject();
							ServerService.this.incomingShapes.add(receivedShape);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						
						DataInputStream _in = null;						
						try {
							_in = new DataInputStream(new BufferedInputStream(accept.getInputStream(), 1024));
							final DataInputStream output = _in;
							handler.post(new Runnable() {
					            public void run() {					               
					               Toast.makeText(getApplicationContext(), "Server got data: " + output, Toast.LENGTH_LONG).show();               
					               //this.run();
					            }
					         });

							Log.d("Server","Got data?: " + _in);
						} catch (IOException e2) {
							e2.printStackTrace();
						}
												
						//Add the shapes we received to incoming shapes.
						
						doNotification(_in);						
						
					}
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e(getClass().getSimpleName(), "Error in Listener", e);
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

	private void doNotification(DataInputStream in) throws IOException {
		String id = in.readUTF();
		displayNotification(id);
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

	public void displayNotification(final String notificationString) {
		Log.d("Notification","Received message from client: " + notificationString);
		handler.post(new Runnable() {
            public void run() {
               Toast.makeText(getApplicationContext(), notificationString, Toast.LENGTH_LONG).show();
            }
         });
	}
	
	public ArrayList<Shape> GetAndDeleteReceivedShapes()
	{
		ArrayList<Shape> shapes = new ArrayList<Shape>(this.incomingShapes);
		this.incomingShapes.removeAll(null);
		return shapes;
	}

}
