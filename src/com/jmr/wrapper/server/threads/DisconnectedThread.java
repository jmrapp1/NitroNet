package com.jmr.wrapper.server.threads;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

/**
 * Networking Library
 * DisconnectedThread.java
 * Purpose: Called when a connection disconnects. Calls the 'disconnected' method of the listener.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class DisconnectedThread implements Runnable {
	
	/** Instance of the listener object. */
	private final SocketListener listener;
	
	/** Instance of the connection. */
	private final Connection con;
	
	/** Creates a new thread to call the disconnect event.
	 * @param listener Instance of the listener object.
	 * @param con Instance of the connection.
	 */
	public DisconnectedThread(SocketListener listener, Connection con) {
		this.listener = listener;
		this.con = con;
	}
	
	@Override
	public void run() {
		if (listener != null)
			listener.disconnected(con);
	}

}
