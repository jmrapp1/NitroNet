package com.jmr.wrapper.server.threads;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.Server;
import com.jmr.wrapper.server.ServerConfig;

/**
 * Networking Library
 * PingThread.java
 * Purpose: There is no way to ensure that a socket is still connected to the server without 
 * sending an object over TCP. If it throws an error it means the socket disconnected and 
 * the connection is removed from the server. The time between sending the ping and whether or
 * not to send the ping can be configured through the ServerConfig class.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class PingThread implements Runnable {

	/** The connection to ping. */
	private final Connection con;
	
	/** Instance of the server. */
	private final Server server;
	
	/** Pings the client to make sure they are still connected.
	 * @param server Instance of the server.
	 * @param con The connection to ping. 
	 */
	public PingThread(Server server, Connection con) {
		this.con = con;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			String s = "TestAlivePing";
			while (!con.getSocket().isClosed()) {
				con.sendTcp(s);
				Thread.sleep(((ServerConfig)server.getConfig()).PING_SLEEP_TIME);
			}
		} catch (NullPointerException | InterruptedException e) {
			server.executeThread(new DisconnectedThread((SocketListener)server.getListener(), con));
			ConnectionManager.getInstance().close(con);
		}
	}
	
}
