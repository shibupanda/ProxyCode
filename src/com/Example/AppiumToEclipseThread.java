package com.Example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
			StringBuilder sb = new StringBuilder();
			while (true) {
				byte[] buffer = new byte[ProxyThread.BUFFER_SIZE];
				int bytesRead = appiumIn.read(buffer);

				if (bytesRead == -1)
					break;

				String bytesToString = new String(buffer, StandardCharsets.UTF_8);

				System.err.print("<<" + bytesToString);
				sb.append(bytesToString);

				eclipseOut.write(buffer, 0, bytesRead);

			}
			System.err.println();
			// System.out.println("<<" + sb.toString());
			eclipseOut.flush();

			System.out.println("AppiumToEclipseThread completed.");
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
