package com.jmr.wrapper.common.exceptions;

/**
 * Networking Library
 * NEClientCantConnect.java
 * Purpose: Thrown when the client is trying to connect to a server but can't.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class NEClientCantConnect extends Exception {

	public NEClientCantConnect() {
		super("Client can't connect to the server at the given IP Address with the given ports.");
	}
	
}
