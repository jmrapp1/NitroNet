package com.jmr.wrapper.common.threads;

import java.net.DatagramSocket;
import java.net.InetAddress;

import com.jmr.wrapper.common.complex.ComplexPiece;

/**
 * Networking Library
 * ComplexUdpSendThread.java
 * Purpose: Sends a complex piece over UDP.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/25/2014
 */

public class ComplexUdpSendThread implements Runnable {

	/** The piece to send. */
	private final ComplexPiece piece;
	
	/** The output stream of the UDP socket. */
	private final DatagramSocket udpOut;
	
	/** The address to send to. */
	private final InetAddress address;
	
	/** The port to send over. */
	private final int port;
	
	/** Creates a new thread to send a piece over TCP.
	 * @param piece The piece to send.
	 * @param udpOut The output stream of the UDP socket. 
	 * @param address The address to send to.
	 * @param port The port to send over.
	 */
	public ComplexUdpSendThread(ComplexPiece piece, DatagramSocket udpOut, InetAddress address, int port) {
		this.piece = piece;
		this.udpOut = udpOut;
		this.address = address;
		this.port = port;
	}
	
	@Override
	public void run() {
		piece.sendUdp(udpOut, address, port);
	}

}
