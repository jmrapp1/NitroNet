package com.jmr.wrapper.common.listener;

import com.jmr.wrapper.common.Connection;

/**
 * Networking Library
 * SocketListener.java
 * Purpose: Interface that lays out the framework for listeners attached to either the Server
 * or Client.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public interface SocketListener extends IListener {

	/** Called when a new packet arrives.
	 * @param con The connection the object came from.
	 * @param object The object that was sent.
	 */
	void received(Connection con, Object object);
	
	/** Called when a new client connects.
	 * @param con The connection of the new client.
	 */
	void connected(Connection con);
	
	/** Called when a client disconnects.
	 * @param con The connection of the client.
	 */
	void disconnected(Connection con);
	
}
