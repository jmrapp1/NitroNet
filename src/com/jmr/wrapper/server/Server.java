package com.jmr.wrapper.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jmr.wrapper.common.NESocket;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.common.exceptions.NECantStartServer;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapper.encryption.Encryptor;
import com.jmr.wrapper.server.threads.TcpAcceptThread;
import com.jmr.wrapper.server.threads.UdpReadThread;

/**
 * Networking Library
 * Server.java
 * Purpose: Starts the a server and manages the TCP and UDP sockets. Provides
 * methods to send packets over UDP and TCP and it also allows a listener to be set to wait 
 * for new connections, disconnections, and packets. It also provides a method to set the 
 * type of encryption being used if any.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class Server implements NESocket {

	/** Used to manage and run threads. */
	private final ExecutorService mainExecutor;
	
	/** The UDP port of the server. */
	private final int udpPort;
	
	/** The TCP socket. */
	private ServerSocket tcpSocket;
	
	/** The UDP socket. */
	private DatagramSocket udpSocket;
	
	/** The listener object. */
	private SocketListener listener;
	
	/** The server configurations. */
	private ServerConfig serverConfig;
	
	/** The type of encryption to use when sending objects or TCP or UDP. */
	private Encryptor encryptionMethod;
	
	/** Starts a new server on the TCP and UDP port.
	 * @param tcpPort The TCP port.
	 * @param udpPort The UDP port.
	 * @throws NECantStartServer 
	 */
	public Server(int tcpPort, int udpPort) throws NECantStartServer {
		serverConfig = new ServerConfig();
		try {
			tcpSocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			throw new NECantStartServer();
		}
		try {
			udpSocket = new DatagramSocket(new InetSocketAddress("localhost", udpPort));
		} catch (SocketException e) {
			throw new NECantStartServer();
		}
		this.udpPort = udpPort;
		mainExecutor = Executors.newCachedThreadPool();
		if (tcpSocket != null && udpSocket != null) {
			ComplexManager.getInstance().setSocket(this);
			mainExecutor.execute(new UdpReadThread(this, udpSocket));
			mainExecutor.execute(new TcpAcceptThread(this, tcpSocket));
		}
	}
	
	/** Sets the listener object.
	 * @param listener The listener.
	 */
	public void setListener(IListener listener) {
		this.listener = (SocketListener) listener;
	}
	
	@Override
	public IListener getListener() {
		return listener;
	}
	
	@Override
	public Encryptor getEncryptionMethod() {
		return encryptionMethod;
	}

	@Override
	public void setEncryptionMethod(Encryptor encryptor) {
		this.encryptionMethod = encryptor;
	}
	
	@Override
	public boolean isConnected() {
		return udpSocket != null && tcpSocket != null;
	}
	
	@Override
	public int getUdpPort() {
		return udpPort;
	}
	
	@Override
	public void executeThread(Runnable run) {
		mainExecutor.execute(run);
	}
	
	@Override
	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}
	
	@Override
	public void close() {
		try {
			if (!tcpSocket.isClosed())
				tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConnectionManager.getInstance().closeAll();
		udpSocket.close();
	}
	
	@Override
	public Config getConfig() {
		return serverConfig;
	}
	
}
