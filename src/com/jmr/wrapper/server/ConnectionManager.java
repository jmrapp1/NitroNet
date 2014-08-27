package com.jmr.wrapper.server;

import java.net.InetAddress;
import java.util.ArrayList;

import com.jmr.wrapper.common.Connection;

/**
 * Networking Library
 * ConnectionManager.java
 * Purpose: Holds all connections currently connected to the server. It is a Singleton and provides
 * methods to get and receive information.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ConnectionManager {

	/** Instance of the class. */
	private static final ConnectionManager instance = new ConnectionManager();
	
	/** An array of all of the connected clients. */
	private final ArrayList<Connection> connections = new ArrayList<Connection>();
	
	/** Private constructor. Used as Singleton. */
	private ConnectionManager() {
		
	}
	
	/** Gets the connection with the set InetAddress.
	 * @param address The address of the connection.
	 * @return The connection.
	 */
	public Connection getConnection(InetAddress address) {
		for (Connection con : connections) {
			if (con.getAddress().equals(address)) {
				return con;
			}
		}
		return null;
	}
	
	/** Gets the connection with the set InetAddress.
	 * @param address The address of the connection.
	 * @return The connection.
	 */
	public Connection getConnection(InetAddress address, int port) {
		for (Connection con : connections) {
			if (con.getAddress().equals(address) && (con.getUdpPort() == port || con.getUdpPort() == -1)) {
				return con;
			}
		}
		return null;
	}
	
	/** Adds a new connection to the list.
	 * @param con The connection.
	 */
	public void addConnection(Connection con) {
		connections.add(con);
	}
	
	/** Closes a specific connection.
	 * @param con The connection to close.
	 */
	public void close(Connection con) {
		con.close();
		connections.remove(con);
	}
	
	/** @return All connections. */
	public ArrayList<Connection> getConnections() {
		return connections;
	}
	
	/** Closes all connections. */
	public void closeAll() {
		for (Connection con : connections) {
			con.close();
		}
	}
	
	/** @return The instance of the class. */
	public static ConnectionManager getInstance() {
		return instance;
	}
	
}
