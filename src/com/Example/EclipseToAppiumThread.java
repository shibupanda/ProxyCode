package com.Example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class EclipseToAppiumThread implements Runnable {

	InputStream eclipseIn;
	OutputStream appiumOut;

	public EclipseToAppiumThread(InputStream eclipseIn, OutputStream appiumOut) {
		this.eclipseIn = eclipseIn;
		this.appiumOut = appiumOut;
	}

	@Override
	public void run() {
		try {
			// Lets read from Eclipse Socket and write the request to Appium Socket
			StringBuilder sb = new StringBuilder();
			StringBuffer sBuf = new StringBuffer();

			while (true) {
				byte[] buffer = new byte[ProxyThread.BUFFER_SIZE];
				int bytesRead = eclipseIn.read(buffer);

				if (bytesRead == -1)
					break;

				String bytesToString = new String(buffer, StandardCharsets.UTF_8);

				System.out.println(">>" + bytesToString);
				sb.append(bytesToString);

				appiumOut.write(buffer, 0, bytesRead);

			}
			System.out.println();
			// System.out.println(">>" + sb.toString());
			appiumOut.flush();

			System.out.println("EclipseToAppiumThread completed.");
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
