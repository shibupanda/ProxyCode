package com.Example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterceptionProxy {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		boolean listening = true;
		int port = 1234;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Port Started on " + port);
		} catch (IOException e) {
			System.out.println("Port Error");
			System.exit(-1);
		}

		

		while (listening) {
			Socket appiumSocket = new Socket("127.0.0.1", 4723);
			new ProxyThread(serverSocket.accept(), appiumSocket).start();
		}
		serverSocket.close();
	}
}

class ProxyThread extends Thread {

	private Socket eclipseSocket = null;
	private Socket appiumSocket = null;

	public static final int BUFFER_SIZE = 100;

	public ProxyThread(Socket eclipseSocket, Socket appiumSocket) {
		super("ProxyThread");
		this.eclipseSocket = eclipseSocket;
		this.appiumSocket = appiumSocket;
	}

	public void run() {

		InputStream eclipseClientIn;
		OutputStream eclipseClientOut = null;
		try {

			eclipseClientIn = eclipseSocket.getInputStream();
			eclipseClientOut = eclipseSocket.getOutputStream();

			InputStream appiumSocketIn = appiumSocket.getInputStream();
			OutputStream appiumSocketOut = appiumSocket.getOutputStream();

			// Lets read from Eclipse Socket and write the request to Appium Socket
			EclipseToAppiumThread e2a = new EclipseToAppiumThread(eclipseClientIn, appiumSocketOut);
			Thread t1 = new Thread(e2a);
			t1.start();

			// Lets Read from AppiumSocket and Write the response to EclipseStream
			AppiumToEclipseThread a2e = new AppiumToEclipseThread(appiumSocketIn, eclipseClientOut);
			Thread t2 = new Thread(a2e);
			t2.start();

			t1.join();
			t2.join();
			
		} catch (IOException | InterruptedException ex) {
			Logger.getLogger(ProxyThread.class.getName()).log(Level.SEVERE, null, ex);

		} finally {
			try {
				appiumSocket.close();
				eclipseSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

}
