package com.jmr.wrapperx.client;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapperx.common.HttpSession;

/**
 * Networking Library
 * HttpConnection.java
 * Purpose: Connects to a Servlet over HTTP protocol. Takes the URL of the location of the
 * servlet to connect.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpConnection {

	/** The URL of the servlet. */
	private final String url;
	
	/** Instance of the client. */
	private final Client client;
	
	/** Client's session that is connected to the servlet. */
	private HttpSession session;
	
	/** Create a new connection to a HTTP servlet. 
	 * @param client Instance of the client.
	 * @param url Location of the servlet.
	 */
	public HttpConnection(Client client, String url) {
		this.client = client;
		this.url = url;
		session = new HttpSession("", client, url);
	}
	
	/** Sends an object to the servlet.
	 * @param object The object to send.
	 */
	public void send(Object object) {
		session.send(object);
	}
	
	/** Sends a complex object to the servlet.
	 * @param object The object to send.
	 */
	public void sendComplex(Object object) {
		session.sendComplex(object);
	}
	
	/** Sends a complex object to the servlet.
	 * @param object The object to send.
	 * @param splitAmount The amount of times to split the object.
	 */
	public void sendComplex(Object object, int splitAmount) {
		session.sendComplex(object, splitAmount);
	}
	
	/** Sets the session of the client if the cookie changes.
	 * @param session The new session.
	 */
	public void setSession(HttpSession session) {
		this.session = session;
	}
	
	/** @return The client's session. */
	public HttpSession getSession() {
		return session;
	}
	
}
