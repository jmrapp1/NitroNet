package com.jmr.wrapper.server.threads;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapperx.common.HttpListener;
import com.jmr.wrapperx.common.HttpSession;

/**
 * Networking Library
 * ReceivedThread.java
 * Purpose: Called when a connection sends a packet. Calls the 'received' method of the listener.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ReceivedThread implements Runnable {
	
	/** Instance of the listener object. */
	private final IListener listener;
	
	/** Instance of the connection. */
	private final IConnection con;
	
	/** The object that was sent. */
	private final Object object;
	
	/** Creates a new thread to call the received event.
	 * @param listener Instance of the listener object.
	 * @param con Instance of the connection.
	 */
	public ReceivedThread(IListener listener, IConnection con, Object object) {
		this.listener = listener;
		this.con = con;
		this.object = object;
	}
	
	@Override
	public void run() {
		if (listener != null) {
			if (listener instanceof SocketListener)
				((SocketListener)listener).received((Connection)con, object);
			else if (listener instanceof HttpListener)
				((HttpListener)listener).received((HttpSession)con, object);
		}
	}

}
