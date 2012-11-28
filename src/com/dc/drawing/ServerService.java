package com.dc.drawing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

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
	public ArrayList<Shape> outgoingShapes;

	private int socketPort;
	
	public String ipAddress;
	
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

		incomingShapes = new ArrayList<Shape>();
		outgoingShapes = new ArrayList<Shape>();

		Log.d("Server.onCreate()", "The server service is starting.");
		Log.d(getClass().getSimpleName(), "onCreate");

		serverThread = new Thread(new Runnable() {
			public void run() {
				try {
					Looper.prepare();
					ss = new ServerSocket(6000);
					Log.d("ip: ", ss.getInetAddress().toString());
					ipAddress = getLocalIpAddress();
		
					while (!stopped) {												
						Socket connection = ss.accept();						
						//Handle the accepted connection. This thread should die.
						//new ServerReceiveHandler(ServerService.this, connection);						
						ObjectInputStream obj_in = new ObjectInputStream(connection.getInputStream());
						
						new ServerSendHandler(ServerService.this, connection);
						
						Shape receivedShape = null;
						try {
							receivedShape = (Shape) obj_in.readObject();
							incomingShapes.add(receivedShape);
						} catch (Exception e) {
							Log.d("ServerService", "No shape received for ReadObject. Supressing EOFException.");
							//e.printStackTrace(); 
						}
						finally {
							obj_in.close();						
							connection.close();
						}
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("ServerService", "Error in Listener", e);
				}

				try {
					ss.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "keep it simple");
				}
			}

		}, "ServerThread");
		//serverThread.start();

	}
	
	 @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
			 String socketPortStr;			 
			 socketPortStr = intent.getStringExtra("port");
			 socketPort = Integer.parseInt(socketPortStr);
			 
			 Log.d("serverSocketPort", String.valueOf(socketPort));			 
			 
			 serverThread.start();
			 
			 return 0;
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

	public void AddShapeToOutgoingList(Shape shapeToAdd) {
		this.outgoingShapes.add(shapeToAdd);
	}
	
	public ArrayList<Shape> GetAndDeleteReceivedShapes() {
		ArrayList<Shape> shapes = new ArrayList<Shape>(this.incomingShapes);
		this.incomingShapes.clear();
		return shapes;
	}
	
	//Do voo-doo to get the, apparently, 'real' ip address.
	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("get-real-ip-sucks", ex.toString());
	    }
	    return null;
	}
}