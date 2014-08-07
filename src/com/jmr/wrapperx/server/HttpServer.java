package com.jmr.wrapperx.server;

import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapper.encryption.IEncryptor;
import com.jmr.wrapper.server.ServerConfig;
import com.jmr.wrapperx.common.HttpListener;

/**
 * Networking Library
 * HttpServer.java
 * Purpose: HTTP server used by the servlet. Waits for incoming clients and stores
 * encryption and configuration information. Singleton object because there can only be
 * one HTTP server running in each application.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpServer implements IProtocol {

	/** Singleton instance of the server. */
	private static final HttpServer instance = new HttpServer();
	
	/** Used to manage and run threads. */
	private final ExecutorService mainExecutor;
	
	/** The listener object. */
	private HttpListener listener;
	
	/** The server configurations. */
	private ServerConfig serverConfig;
	
	/** The type of encryption to use when sending objects over HTTP protocol. */
	private IEncryptor encryptionMethod;
	
	/** Starts a new HTTP server with the default settings. */
	private HttpServer() {
		mainExecutor = Executors.newCachedThreadPool();
		serverConfig = new ServerConfig();
		ComplexManager.getInstance().setProtocol(this);
	}

	@Override
	public void executeThread(Runnable run) {
		mainExecutor.execute(run);
	}
	
	@Override
	public int getUdpPort() {
		return -1;
	}

	@Override
	public DatagramSocket getUdpSocket() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public Config getConfig() {
		return serverConfig;
	}

	/** Sets the listener object.
	 * @param listener The listener.
	 */
	public void setListener(IListener listener) {
		this.listener = (HttpListener) listener;
	}
	
	@Override
	public IListener getListener() {
		return listener;
	}
	
	@Override
	public IEncryptor getEncryptionMethod() {
		return encryptionMethod;
	}

	@Override
	public void setEncryptionMethod(IEncryptor encryptor) {
		this.encryptionMethod = encryptor;
	}

	@Override
	public void close() {
				
	}
	
	/** @return The instance of the HTTP Server. */
	public static HttpServer getInstance() {
		return instance;
	}
	
	/** Sets the Listener and returns the instance.
	 * @param listener The listener to set. 
	 * @return Instance of the HTTP server.
	 */
	public static HttpServer getInstance(IListener listener) {
		instance.setListener(listener);
		return instance;
	}
	
}
