package com.Example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
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
			StringBuffer sb = new StringBuffer();
			String line = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(eclipseIn, StandardCharsets.UTF_8));

			// read headers
			while ((line = br.readLine()) != null) {
				sb.append(line);

				byte[] lineBytes = (line + "\r\n").getBytes(StandardCharsets.UTF_8);
				System.out.println("  >>  " + line);
				appiumOut.write(lineBytes);
				if (line.isEmpty())
					break;
			}

			System.out.println("Now will read Body");
			// System.out.println(">>" + sb.toString());
			appiumOut.flush();

			while (true) {
				CharBuffer buffer = CharBuffer.allocate(ProxyThread.BUFFER_SIZE);
				int bytesRead = br.read(buffer);

				String bytesToString = new String(buffer.array());

				System.out.println("  >>  " + bytesToString);
				sb.append(bytesToString);

				appiumOut.write(bytesToString.getBytes(StandardCharsets.UTF_8), 0, bytesRead);

				if (bytesRead < ProxyThread.BUFFER_SIZE)
					break;
			}

			System.out.println("EclipseToAppiumThread completed.");
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
