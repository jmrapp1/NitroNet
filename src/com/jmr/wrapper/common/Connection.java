package com.jmr.wrapper.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import com.jmr.wrapper.common.complex.ComplexObject;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.common.utils.PacketUtils;
import com.jmr.wrapper.server.ConnectionManager;

/**
 * Networking Library
 * Connection.java
 * Purpose: Holds the TCP and UDP information on both the client and server side. It takes care
 * of sending packets and encrypting them. 
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class Connection implements IConnection {

	/** Counter used to set the id of connections. */
	private static int counter = 0;
	
	/** The id of the connection. */
	private final int id;
	
	/** The port to UDP connection. */
	private int port = -1;
	
	/** The InetAddress of the connection. */
	private transient final InetAddress address;
	
	/** Instance of the UDP socket. */
	private transient DatagramSocket udpSocket;
	
	/** Instance of the TCP socket. */
	private transient Socket socket;
	
	/** Instance of the TCP Object Output Stream. */
	private transient ObjectOutputStream tcpOut;
	
	/** The amount of UDP packets received that were corrupted. */
	private int packetsLost = 0;
	
	/** Instance of the protocol. */
	private IProtocol protocol;
	
	/** Creates a new connection.
	 * @param port Instance of the UDP port.
	 * @param socket Instance of the TCP socket.
	 * @param udpSocket Instance of the UDP socket.
	 */
	public Connection(int port, Socket socket, DatagramSocket udpSocket) {
		this.port = port;
		this.socket = socket;
		this.udpSocket = udpSocket;
		address = socket.getInetAddress();

		try {
			socket.setSoLinger(true, 0);
			tcpOut = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		id = ++counter;
	}	
	
	/** @return The address to the connection. */
	public InetAddress getAddress() {
		return address;
	}
	
	/** @return The TCP socket. */
	public Socket getSocket() {
		return socket;
	}
	
	/** @return The id of the connection. */
	public int getId() {
		return id;
	}
	
	/** Sets the UDP port of the connection. Used to send UDP packets to the connection.
	 * @param port The UDP port.
	 */
	public void setUdpPort(int port) {
		this.port = port;
	}
	
	/** @return The port UDP is bound to. */
	public int getUdpPort() {
		return port;
	}
	
	public boolean isConnected() {
		return udpSocket != null && socket != null && socket.isConnected() && socket.isBound() && !socket.isClosed();
	}
	
	/** Sends an object over the UDP socket.
	 * @param object The object to send.
	 */
	public void sendUdp(Object object) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOutStream);
			objOut.writeObject(object);
			byte[] data = PacketUtils.getByteArray(protocol, byteOutStream);

			DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
			udpSocket.send(sendPacket);
			objOut.flush();
			byteOutStream.flush();
			objOut.close();
			byteOutStream.close();
		} catch (IOException e) {
			if (protocol.getListener() != null && protocol.getListener() instanceof SocketListener)
				((SocketListener)protocol.getListener()).disconnected(this);
			ConnectionManager.getInstance().close(this);
			e.printStackTrace();
		}
	}
	
	/** Sends an object over the TCP socket.
	 * @param object The object to send.
	 */	
	public void sendTcp(Object object) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOutStream);
			objOut.writeObject(object);
			byte[] data = PacketUtils.getByteArray(protocol, byteOutStream);
			objOut.close();
			byteOutStream.close();
			synchronized(tcpOut) {
				tcpOut.write(data);
				tcpOut.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (protocol.getListener() != null && protocol.getListener() instanceof SocketListener)
				((SocketListener)protocol.getListener()).disconnected(this);
			ConnectionManager.getInstance().close(this);
		}
	}
	
	/** Sends an object over TCP by splitting it into separate packets.
	 * @param object The object to send.
	 * @param splitAmount The amount of splits to make
	 */
	public void sendComplexObjectTcp(Object object) {
		sendComplexObjectTcp(object, 3);
	}
	
	/** Sends an object over TCP by splitting it into separate packets.
	 * @param object The object to send.
	 * @param splitAmount The amount of splits to make
	 */
	public void sendComplexObjectTcp(Object object, int splitAmount) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOutStream);
			objOut.writeObject(object);
			byte[] checksum = PacketUtils.getChecksumOfObject(byteOutStream.toByteArray()).getBytes();
			byte[] data =  PacketUtils.getCompressedByteArray(protocol, byteOutStream);
			objOut.flush();
			objOut.close();
			byteOutStream.close();
			new ComplexObject(data, checksum, protocol, splitAmount).sendTcp(tcpOut);
		} catch (IOException e) {
			e.printStackTrace();
			if (protocol.getListener() != null && protocol.getListener() instanceof SocketListener)
				((SocketListener)protocol.getListener()).disconnected(this);
			ConnectionManager.getInstance().close(this);
		}
	}
	
	/** Sends an object over UDP by splitting it into 3 separate packets.
	 * @param object The object to send.
	 */
	public void sendComplexObjectUdp(Object object) {
		sendComplexObjectUdp(object, 3);
	}
	
	/** Sends an object over UDP by splitting it into separate packets.
	 * @param object The object to send.
	 * @param splitAmount The amount of splits to make
	 */
	public void sendComplexObjectUdp(Object object, int splitAmount) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOutStream);
			objOut.writeObject(object);
			byte[] checksum = PacketUtils.getChecksumOfObject(byteOutStream.toByteArray()).getBytes();
			byte[] data = PacketUtils.getCompressedByteArray(protocol, byteOutStream);
			objOut.flush();
			objOut.close();
			byteOutStream.close();
			new ComplexObject(data, checksum, protocol, splitAmount).sendUdp(udpSocket, address, port);
		} catch (IOException e) {
			e.printStackTrace();
			if (protocol.getListener() != null && protocol.getListener() instanceof SocketListener)
				((SocketListener)protocol.getListener()).disconnected(this);
			ConnectionManager.getInstance().close(this);
		}
	}

	/** Adds one to the amount of UDP packets lost. */
	public void addPacketLoss() {
		packetsLost++;
		System.out.println("Lost packet. Amount lost: " + packetsLost);
	}
	
	/** @return The amount of packets lost. */
	public int getPacketsLost() {
		return packetsLost;
	}
	
	/** @return The TCP Object Output Stream. */
	public ObjectOutputStream getTcpOutputStream() {
		return tcpOut;
	}
	
	/** Sets the instance of the protocol being used. */
	public void setProtocol(IProtocol protocol) {
		this.protocol = protocol;
	}
	
	/** Closes the connection. */
	public void close() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			//socket already closed
		}
		socket = null;
	}
}
