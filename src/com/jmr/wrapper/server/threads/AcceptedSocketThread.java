package com.jmr.wrapper.server.threads;

import java.net.Socket;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.Server;
import com.jmr.wrapper.server.ServerConfig;

/**
 * Networking Library
 * AcceptedSocketThread.java
 * Purpose: Called when a new Socket connects to the server. Adds the connection to the 
 * ConnectionManager, calls the 'connected' method of the listener, starts a ping thread to 
 * make sure the connection stays alive, and starts a thread to listen to incoming packets
 * over TCP.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class AcceptedSocketThread implements Runnable {

	/** Instance of the TCP socket. */
	private final Socket socket;
	
	/** Instance of the Server object. */
	private final Server server;

	/** Creates a new thread when a new socket arrives.
	 * @param server Instance of the server.
	 * @param socket Instance of the socket.
	 */
	public AcceptedSocketThread(Server server, Socket socket) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		Connection con = new Connection(-1, socket, server.getUdpSocket());
		con.setProtocol(server);
		ConnectionManager.getInstance().addConnection(con);
		server.executeThread(new NewConnectionThread(server.getListener(), con));
		if (((ServerConfig)server.getConfig()).PING_CLIENTS)
			server.executeThread(new PingThread(server, con));
		server.executeThread(new ServerTcpReadThread(server, con));
	}
}
