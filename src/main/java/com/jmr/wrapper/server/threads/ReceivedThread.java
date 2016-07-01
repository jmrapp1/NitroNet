package com.jmr.wrapper.server.threads;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

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
	private final SocketListener listener;
	
	/** Instance of the connection. */
	private final Connection con;
	
	/** The object that was sent. */
	private final Object object;
	
	/** Creates a new thread to call the received event.
	 * @param listener Instance of the listener object.
	 * @param con Instance of the connection.
	 */
	public ReceivedThread(SocketListener listener, Connection con, Object object) {
		this.listener = listener;
		this.con = con;
		this.object = object;
	}
	
	@Override
	public void run() {
		if (listener != null) {
			listener.received(con, object);
		}
	}

}
