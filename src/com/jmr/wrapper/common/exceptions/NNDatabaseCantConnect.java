package com.jmr.wrapper.common.exceptions;

/**
 * Networking Library
 * NEDatabaseCantConnect.java
 * Purpose: Thrown when unable to connect to a database.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class NNDatabaseCantConnect extends Exception {
	
	public NNDatabaseCantConnect() {
		super("Can't make a connection to the database. Make sure all of the parameters are correct.");
	}
	
}
