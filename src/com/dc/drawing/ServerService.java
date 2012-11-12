package com.dc.drawing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class ServerService extends Service {

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
						
						Log.d("ip: ", accept.getInetAddress().toString()); 						
						Log.d("Server Loop", "Loop.");
						
						DataInputStream _in = null;
						try {
							_in = new DataInputStream(new BufferedInputStream(
									accept.getInputStream(), 1024));
						} catch (IOException e2) {
							e2.printStackTrace();
						}

						//int method = _in.readInt();

						//switch (method) {
						// notification
						//case 1:
							doNotification(_in);
							//break;
						//}
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

	public void displayNotification(String notificationString) {
		// int icon = R.drawable.mp_warning_32x32_n;

		CharSequence contentTitle = notificationString;
		CharSequence contentText = "Hello World!";

		Toast.makeText(getApplicationContext(),
				(String) contentTitle + "\n" + contentText, Toast.LENGTH_LONG)
				.show();
	}

}
