package com.jmr.wrapper.common;

import java.net.DatagramSocket;

import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapper.encryption.IEncryptor;

/**
 * Networking Library
 * IProtocol.java
 * Purpose: Interface that is implemented by the TCP and UDP server and client, and also by the HttpServer. Provides methods that both
 * need to have so that threads can have access to their information regardless of whether it is
 * the server or client side.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public interface IProtocol {

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
	void setEncryptionMethod(IEncryptor encryptor);
	
	/** @return The encryption method. */
	IEncryptor getEncryptionMethod();
	
	/** Closes the socket. */
	void close();
	
}
