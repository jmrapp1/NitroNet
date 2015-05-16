package com.jmr.wrapper.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jmr.wrapper.client.threads.ClientTcpReadThread;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.common.listener.IListener;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.encryption.IEncryptor;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.threads.UdpReadThread;
import com.jmr.wrapperx.client.HttpConnection;

/**
 * Networking Library
 * Client.java
 * Purpose: Starts a connection to a server and manages the TCP and UDP sockets. Provides
 * methods to send packets over UDP and TCP and it also allows a listener to be set to wait 
 * for new connections, disconnections, and packets. It also provides a method to set the 
 * type of encryption being used if any.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class Client implements IProtocol {

	/** The TCP and UDP port. */
	private final int tcpPort, udpPort;
	
	/** The InetAddress of the server to connect to. */
	private InetAddress address;
	
	/** The executor for all threads. */
	private ExecutorService mainExecutor;
	
	/** The socket used to send packets over TCP. */
	private Socket tcpSocket;
	
	/** The socket used to send packets over UDP. */
	private DatagramSocket udpSocket;
	
	/** The listener object. */
	private IListener listener;
	
	/** The connection to the server. */
	private Connection serverConnection;
	
	/** The client-sided configurations. */
	private ClientConfig clientConfig;
	
	/** The type of encryption to use when sending and receiving packets. */
	private IEncryptor encryptionMethod;

	/** Connection to the HTTP Server if set. */
	private HttpConnection httpConnection;
	
	/** Creates a new client sets the variables to be used to connect to a server later.
	 * @param address The address to the server.
	 * @param tcpPort The TCP port.
	 * @param udpPort The UDP port.
	 */
	public Client(String address, int tcpPort, int udpPort) {
		try {
			if (address.equalsIgnoreCase("localhost"))
				address = InetAddress.getLocalHost().getHostName();
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		clientConfig = new ClientConfig();
	}
	
	public Client() {
		address = null;
		clientConfig = new ClientConfig();
		tcpPort = -1;
		udpPort = -1;
		mainExecutor = Executors.newCachedThreadPool();
	}
	
	/** Connects to the server. */
	public void connect() {
		try {
			udpSocket = new DatagramSocket();
			tcpSocket = new Socket(address, tcpPort);
			//tcpSocket.connect(address);
			serverConnection = new Connection(udpPort, tcpSocket, udpSocket);
			serverConnection.setProtocol(this);
			ComplexManager.getInstance().setProtocol(this);
			if (listener != null) {
				((SocketListener)listener).connected(serverConnection);
			}
			ConnectionManager.getInstance().addConnection(serverConnection);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		mainExecutor = Executors.newCachedThreadPool();
		
		if (tcpSocket != null && tcpSocket.isConnected() && udpSocket != null) {
			mainExecutor.execute(new UdpReadThread(this, udpSocket));
			mainExecutor.execute(new ClientTcpReadThread(this, serverConnection));
			serverConnection.sendTcp(new String("ConnectedToServer"));
			serverConnection.sendUdp(new String("SettingUdpPort"));
		}
	}
	
	/** Starts a connection to a URL containing a servlet. Sends a small byte to
	 * initiate the client's cookies. Sleeps for 2 seconds to wait for client cookies
	 * to be set.
	 * @param url The servlets URL.
	 */
	public void setHttpConnection(String url) {
		httpConnection = new HttpConnection(this, url);
		httpConnection.send(new String("ConnectionSetup"));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** @return The servlet connection. */
	public HttpConnection getHttpConnection() {
		return httpConnection;
	}
	
	/** Sets the listener object.
	 * @param listener The listener.
	 */
	public void setListener(IListener listener) {
		this.listener = listener;
	}
	
	@Override
	public IListener getListener() {
		return listener;
	}
	
	@Override
	public Config getConfig() {
		return clientConfig;
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
	public boolean isConnected() {
		return udpSocket != null && tcpSocket != null && tcpSocket.isConnected() && tcpSocket.isBound() && !tcpSocket.isClosed();
	}
	
	@Override
	public void executeThread(Runnable run) {
		mainExecutor.execute(run);
	}
	
	@Override
	public int getUdpPort() {
		return udpPort;
	}
	
	@Override
	public void close() {
		try {
			tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		udpSocket.close();
	}
	
	/** @return The connection to the server. */
	public Connection getServerConnection() {
		return serverConnection;
	}
	
	@Override
	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}
	
}
