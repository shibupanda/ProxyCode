package com.Example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class AppiumToEclipseThread implements Runnable {

	InputStream appiumIn;
	OutputStream eclipseOut;

	public AppiumToEclipseThread(InputStream appiumIn, OutputStream eclipseOut) {
		this.appiumIn = appiumIn;
		this.eclipseOut = eclipseOut;
	}

	@Override
	public void run() {
		try {
			// Lets read from Eclipse Socket and write the request to Appium Socket
			StringBuffer sb = new StringBuffer();
			String line = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(appiumIn, StandardCharsets.UTF_8));

			// read headers
			while ((line = br.readLine()) != null) {
				sb.append(line);

				byte[] lineBytes = (line + "\r\n").getBytes(StandardCharsets.UTF_8);
				System.err.println("  <<  " + line);
				eclipseOut.write(lineBytes);
				if (line.isEmpty())
					break;
			}

			System.err.println("Now will read Body");
			// System.out.println(">>" + sb.toString());
			eclipseOut.flush();

			while (true) {
				CharBuffer buffer = CharBuffer.allocate(ProxyThread.BUFFER_SIZE);
				int bytesRead = br.read(buffer);

				String bytesToString = new String(buffer.array());

				System.err.println("  <<  " + bytesToString);
				sb.append(bytesToString);

				eclipseOut.write(bytesToString.getBytes(StandardCharsets.UTF_8), 0, bytesRead);

				if (bytesRead < ProxyThread.BUFFER_SIZE)
					break;
			}

			System.err.println("AppiumToEclipseThread completed.");
			System.err.println();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
