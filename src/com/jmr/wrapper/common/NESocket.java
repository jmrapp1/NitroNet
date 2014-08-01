package com.jmr.wrapper.common;

import java.net.DatagramSocket;

import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.encryption.Encryptor;

/**
 * Networking Library
 * NESocket.java
 * Purpose: Interface that is implemented by the Server and Client. Provides methods that both
 * need to have so that threads can have access to their information regardless of whether it is
 * the Server or Client side.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public interface NESocket {

	/** Executes a new thread.
	 * @param run The thread.
	 */
	void executeThread(Runnable run);
	
	/** @return The listener object. */
	IListener getListener();
	
	/** @return The UDP port. */
	int getUdpPort();
	
	/** @return The UDP socket. */
	DatagramSocket getUdpSocket();
	
	/** @return Whether it is connected. */
	boolean isConnected();

	/** Returns the configuration settings. */
	Config getConfig();

	/** Sets the encryption method. */
	void setEncryptionMethod(Encryptor encryptor);
	
	/** @return The encryption method. */
	Encryptor getEncryptionMethod();
	
	/** Closes the socket. */
	void close();
	
}
