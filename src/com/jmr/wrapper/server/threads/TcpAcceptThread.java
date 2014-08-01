package com.jmr.wrapper.server.threads;

import java.io.IOException;
import java.net.ServerSocket;

import com.jmr.wrapper.server.Server;

/**
 * Networking Library
 * TcpAcceptThread.java
 * Purpose: Uses the ServerSocket from the Server to wait for Sockets to connect. Starts
 * a new AcceptedSocketThread when a Socket connects.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class TcpAcceptThread implements Runnable {

	/** Instance of the Server's TCP Socket. */
	private final ServerSocket tcpSocket;
	
	/** Instance of the Server. */
	private final Server server;
	
	/** Waits for new sockets to connect. 
	 * @param server Instance of the server.
	 * @param tcpSocket Instance of the Server's TCP Socket. 
	 */
	public TcpAcceptThread(Server server, ServerSocket tcpSocket) {
		this.tcpSocket = tcpSocket;
		this.server = server;
	}
	
	@Override
	public void run() {
		while (!tcpSocket.isClosed()) {
			try {
				server.executeThread(new AcceptedSocketThread(server, tcpSocket.accept()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
