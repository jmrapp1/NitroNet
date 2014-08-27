package com.jmr.wrapper.server.threads;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapperx.common.HttpListener;
import com.jmr.wrapperx.common.HttpSession;

/**
 * Networking Library
 * NewConnectionThread.java
 * Purpose: Called when a new connection connects to the server. Calls the 'connected' method
 * of the listener.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class NewConnectionThread implements Runnable {
	
	/** Instance of the listener object. */
	private final IListener listener;
	
	/** Instance of the connection. */
	private final IConnection con;
	
	private final HttpSession httpSession;
	
	/** Creates a new thread to call the new connection event.
	 * @param listener Instance of the listener object.
	 * @param con Instance of the connection.
	 */
	public NewConnectionThread(IListener listener, IConnection con) {
		this.listener = listener;
		this.con = con;
		httpSession = null;
	}
	
	@Override
	public void run() {
		if (listener != null) {
			if (listener instanceof SocketListener)
				((SocketListener)listener).connected((Connection)con);
			else if (listener instanceof HttpListener)
				((HttpListener)listener).connected((HttpSession)con);
		}
	}

}
