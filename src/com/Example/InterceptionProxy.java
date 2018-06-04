package com.Example;

import java.net.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterceptionProxy {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		boolean listening = true;
		int port = 1234;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Port Started on "+port);
		} catch (IOException e) {
			System.out.println("Port Error");
			System.exit(-1);
		}
		while (listening) {
			new ProxyThread(serverSocket.accept()).start();
		}
		serverSocket.close();
	}
}

class ProxyThread extends Thread {

	private Socket socket = null;
	private static final int BUFFER_SIZE = 32768;

	public ProxyThread(Socket socket) {
		super("ProxyThread");
		this.socket = socket;
	}

	public void run() {
		PrintWriter outGoing = null;
		DataInputStream clientin;
		DataOutputStream clientout = null;
		try {
			
//			InputStream in= socket.getInputStream();
//			OutputStream out = socket.getOutputStream();
//			
//			String result = getStringFromInputStream(in);
//
//			System.out.println(result);
//			System.out.println("Done");
			
			
			clientin = new DataInputStream(socket.getInputStream());
			
			
			
			clientout = new DataOutputStream(socket.getOutputStream());

			
			
			
			
			Socket connSocket = new Socket("localhost", 4723);
			
			DataInputStream connSocketIn = new DataInputStream(connSocket.getInputStream());
			DataOutputStream connSocketOut = new DataOutputStream(connSocket.getOutputStream());
			
//			InputStream outIn = connSocket.getInputStream();
//			String outRes=getStringFromInputStream(outIn);
//			
//			System.out.println(outRes);
			
			
//			outGoing = new PrintWriter(socket.getOutputStream(), true);
//			BufferedReader inComing = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String incomingRequest;
			String url = "";
			String request = "";
			String response = "";
			
			//Take the incoming request
			byte[] buff = new byte[1024];
		    int a = 0;
		    while((a = clientin.read(buff)) > -1) {

		        // a is the number of bytes ACTUALLY read, so 
		        // when we write, that's the number of bytes to write
		    	connSocketOut.write(buff,0,a);
		    	
		    }
		    connSocketOut.flush();
			

			byte[] buffer = new byte[1024];
			int b =0;
		
			while(( b=connSocketIn.read(buffer))>-1) {
				clientout.write(buffer, 0, b);
				
			}
		    
			clientout.flush();
		    
			
		} catch (IOException ex) {
			Logger.getLogger(ProxyThread.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				clientout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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


