package com.jmr.wrapper.common.exceptions;

public class NNCantStartServer extends Exception {

	public NNCantStartServer() {
		super("Can't start the server socket on the desired port. Is something already binded to it?");
	}
	
}
