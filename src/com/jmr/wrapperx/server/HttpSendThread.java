package com.jmr.wrapperx.server;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Networking Library
 * HttpSendThread.java
 * Purpose: Used by a servlet to send objects over an output stream to a client.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpSendThread implements Runnable {

	/** The data to send. */
	private final byte[] data;
	
	/** The output stream of the servlet's response. */
	private final BufferedOutputStream out;
	
	/** Sends a byte array over to the client.
	 * @param data The data to send.
	 * @param out The output stream of the response.
	 */
	public HttpSendThread(byte[] data, BufferedOutputStream out) {
		this.data = data;
		this.out = out;
	}
	
	@Override
	public void run() {
		try {
			out.write(data);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
