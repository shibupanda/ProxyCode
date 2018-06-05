package com.Example;

import java.net.*;
import java.io.*;
import java.nio.CharBuffer;
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

		Socket appiumSocket = new Socket("127.0.0.1", 4723);

		while (listening) {
			new ProxyThread(serverSocket.accept(), appiumSocket).start();
		}
		serverSocket.close();
	}
}

class ProxyThread extends Thread {

	private Socket eclipseSocket = null;
	private Socket appiumSocket = null;

	private static final int BUFFER_SIZE = 1024;

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
			BufferedReader reader = new BufferedReader(new InputStreamReader(eclipseClientIn));
			String line;
			int contentLength = 0;
			// read Headers
			while ((line = reader.readLine()) != null) {
				System.out.println(">>> " + line);
				
				byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
				appiumSocketOut.write(lineBytes, 0, lineBytes.length);
								
				if (line.startsWith("Content-Length: "))
					contentLength = Integer.parseInt(line.split("Content-Length: ")[1]);

				if (line.isEmpty())
					break;
			}

			// Now read Body
			while (contentLength > 0) {
				System.out.println("Remaining Content-Length: " + contentLength);
				byte[] buffer = new byte[BUFFER_SIZE];

				int readLength = eclipseClientIn.read(buffer);
				appiumSocketOut.write(buffer, 0, readLength);
				contentLength -= readLength;

			}

			appiumSocketOut.flush();

			System.out.println("Reading completed. Now Writing");

			// Lets Read from AppiumSocket and Write the response to EclipseStream
			byte[] buffer = new byte[BUFFER_SIZE];
			int b = 0;
			while ((b = appiumSocketIn.read(buffer)) > -1) {
				System.out.println("########Executed#############");
				eclipseClientOut.write(buffer, 0, b);

				System.out.println("<<< " + new String(buffer, StandardCharsets.UTF_8));

			}
			eclipseClientOut.flush();
			System.out.println();

		} catch (IOException ex) {
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
