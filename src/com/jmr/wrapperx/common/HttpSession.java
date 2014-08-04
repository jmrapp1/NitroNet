package com.jmr.wrapperx.common;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.ConnectionUtils;
import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.NESocket;
import com.jmr.wrapperx.client.HttpPostThread;
import com.jmr.wrapperx.server.HttpSendThread;

/**
 * Networking Library
 * HttpSession.java
 * Purpose: A client's session connected to a servlet. Used by the server and client. Gives
 * the method needed to send objects over HTTP. Holds cookie information to keep track of
 * clients on the server.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpSession implements IConnection {

	/** The ID increment value of each session. */
	private static int ID_INCREMENT = 0;
	
	/** The ID of the session. */
	private final int id;
	
	/** Instance of the NESocket used to encrypt/decrypt data. */
	private final NESocket neSocket;
	
	/** A client's cookie data. */
	private final String cookie;
	
	/** URL of the location of the servlet. */
	private final String url;
	
	/** Output stream of the session. Changes each request to the servlet. */
	private BufferedOutputStream out;
	
	/** A client's session stored on the servlet or client side. Holds user information
	 * and allows objects to be sent over a stream.
	 * 
	 * @param cookie The cookie of the session.
	 * @param neSocket Instance of the NESocket for encryption/decryption.
	 * @param url URL of the servlet.
	 */
	public HttpSession(String cookie, NESocket neSocket, String url) {
		this.neSocket = neSocket;
		this.url = url;
		this.cookie = cookie;
		id = ID_INCREMENT++;
	}
	
	/** @return The session's cookie. */
	public String getCookie() {
		return cookie;
	}
	
	/** Sets the output stream to the client.
	 * @param out The output stream.
	 */
	public void setOutputStream(BufferedOutputStream out) {
		this.out = out;
	}
	
	/** Sends an object over HTTP protocol. 
	 * @param object The object to send.
	 */
	public void send(Object object) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOutStream);
			objOut.writeObject(object);
			byte[] data = ConnectionUtils.getByteArray(neSocket, byteOutStream, object);
			if (neSocket instanceof Client && url != null) {
				if (cookie == null)
					neSocket.executeThread(new Thread(new HttpPostThread((Client)neSocket, url, data)));
				else
					neSocket.executeThread(new Thread(new HttpPostThread((Client)neSocket, url, data, cookie)));
			} else {
				new Thread(new HttpSendThread(data, out)).run();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
