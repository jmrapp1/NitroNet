package com.jmr.wrapper.common.exceptions;

public class NECantStartServer extends Exception {

	public NECantStartServer() {
		super("Can't start the server socket on the desired port. Is something already binded to it?");
	}
	
}
