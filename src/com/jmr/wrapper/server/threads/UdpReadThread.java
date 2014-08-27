package com.jmr.wrapper.server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.server.ConnectionManager;

/**
 * Networking Library
 * UdpReadThread.java
 * Purpose: Waits for incoming packets over UDP, decrypts the data if an encryptor is set, and 
 * checks if the checksums match to ensure that the objects are not corrupt.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class UdpReadThread implements Runnable {

	/** Instance of the UDP Socket. */
	private DatagramSocket udpSocket;
	
	/** Instance of the Socket. */
	private final IProtocol protocol;
	
	/** Creates a new thread to wait for UDP packets on the connection.
	 * @param socket Instance of the socket.
	 * @param udpSocket Instance of the UDP socket. 
	 */
	public UdpReadThread(IProtocol protocol, DatagramSocket udpSocket) {
		this.udpSocket = udpSocket;
		this.protocol = protocol;
	}
	
	@Override
	public void run() { 
		while (udpSocket != null) {
			try {
				byte[] incomingData = new byte[protocol.getConfig().PACKET_BUFFER_SIZE];
				DatagramPacket readPacket = new DatagramPacket(incomingData, incomingData.length);
				udpSocket.receive(readPacket);
				Connection con = ConnectionManager.getInstance().getConnection(readPacket.getAddress(), readPacket.getPort());
				if (con == null) {
					System.out.println("Connection tried sending a packet without being connected to TCP.");
					return;
				}
				protocol.executeThread(new UdpHandleThread(protocol, con, readPacket));
			} catch (IOException e) {
				udpSocket = null;
				protocol.close();
				e.printStackTrace();
			}
		}
	}
}
