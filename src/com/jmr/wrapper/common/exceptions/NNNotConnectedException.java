package com.jmr.wrapper.common.exceptions;

/**
 * Networking Library
 * NotConnectedException.java
 * Purpose: Called when the client is not connected to the TCP socket but it try to send/receive
 * packets.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class NNNotConnectedException extends Exception {

	public NNNotConnectedException() {
		super("Client is not connected through TCP.");
	}
	
}
