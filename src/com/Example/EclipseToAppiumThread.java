package com.Example;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class EclipseToAppiumThread implements Runnable {

	InputStream eclipseIn;
	OutputStream appiumOut;

	private Boolean delayedWrite = false;

	public EclipseToAppiumThread(InputStream eclipseIn, OutputStream appiumOut) {
		this.eclipseIn = eclipseIn;
		this.appiumOut = appiumOut;
	}

	@Override
	public void run() {
		try {
			// Lets read from Eclipse Socket and write the request to Appium Socket
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			String line = null;

			BufferedReader br = new BufferedReader(new InputStreamReader(eclipseIn, StandardCharsets.UTF_8));

			// read headers
			while ((line = br.readLine()) != null) {
				line = line + "\r\n";

				byte[] lineBytes = (line).getBytes(StandardCharsets.UTF_8);
				bais.write(lineBytes);

				if (!delayedWrite) {
					appiumOut.write(lineBytes);
					System.out.print(" >> " + line);
				}

				if (line.isEmpty() || line.contentEquals("\r\n"))
					break;
			}

			System.out.println("Now will read Body");

			while (true) {
				CharBuffer buffer = CharBuffer.allocate(ProxyThread.BUFFER_SIZE);
				int bytesRead = br.read(buffer);

				String bytesToString = new String(buffer.array());

				bais.write(bytesToString.getBytes(StandardCharsets.UTF_8), 0, bytesRead);
				if (!delayedWrite) {
					appiumOut.write(bytesToString.getBytes(StandardCharsets.UTF_8), 0, bytesRead);
					System.out.println(" >> " + bytesToString);
				}

				if (bytesRead < ProxyThread.BUFFER_SIZE)
					break;
			}

			if (delayedWrite) {
				// this means we need to process the data before sending to Appium
				// Intercept Here - Yahan pe KUCH-KUCH karna hai

				Thread.sleep(10000);

				appiumOut.write(bais.toByteArray());
				System.out.println(" >> " + new String(bais.toByteArray(), StandardCharsets.UTF_8));
			}

			appiumOut.flush();

			System.out.println(Thread.currentThread().getName() + " " + "EclipseToAppiumThread completed.");
			System.out.println();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
