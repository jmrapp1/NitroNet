package com.jmr.wrapper.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jmr.wrapper.client.threads.ClientTcpReadThread;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IListener;
import com.jmr.wrapper.common.NESocket;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.config.Config;
import com.jmr.wrapper.encryption.Encryptor;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.threads.UdpReadThread;

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

public class Client implements NESocket {

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
	private Encryptor encryptionMethod;
	
	/** Creates a new client sets the variables to be used to connect to a server later.
	 * @param address The address to the server.
	 * @param tcpPort The TCP port.
	 * @param udpPort The UDP port.
	 */
	public Client(String address, int tcpPort, int udpPort) {
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		clientConfig = new ClientConfig();
	}
	
	/** Connects to the server. */
	public void connect() {
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			tcpSocket = new Socket(address, tcpPort);
			tcpSocket.setSoLinger(true, 0);
			serverConnection = new Connection(udpPort, tcpSocket, udpSocket);
			serverConnection.setNESocketInstance(this);
			ComplexManager.getInstance().setSocket(this);
			ConnectionManager.getInstance().addConnection(serverConnection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mainExecutor = Executors.newCachedThreadPool();
		
		if (tcpSocket != null && tcpSocket.isConnected() && udpSocket != null) {
			mainExecutor.execute(new UdpReadThread(this, udpSocket));
			mainExecutor.execute(new ClientTcpReadThread(this, serverConnection));
			sendTcp(new String("ConnectedToServer"));
			sendUdp(new String("SettingUdpPort"));
		}
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
	public Encryptor getEncryptionMethod() {
		return encryptionMethod;
	}

	@Override
	public void setEncryptionMethod(Encryptor encryptor) {
		this.encryptionMethod = encryptor;
	}
	
	@Override
	public boolean isConnected() {
		return udpSocket != null && tcpSocket != null && tcpSocket.isConnected();
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
	
	/** Sends a packet to the server over UDP
	 * @param object The object to send.
	 */
	public void sendUdp(Object object) {
		serverConnection.sendUdp(object);
	}
	
	/** Sends a packet to the server over TCP
	 * @param object The object to send.
	 */
	public void sendTcp(Object object) {
		serverConnection.sendTcp(object);
	}

	@Override
	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}
	
}
