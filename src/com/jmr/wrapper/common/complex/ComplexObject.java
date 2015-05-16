package com.jmr.wrapper.common.complex;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.threads.ComplexUdpSendThread;

/**
 * Networking Library
 * ComplexObject.java
 * Purpose: An object that takes another object's byte array and splits it into pieces. It then sends these pieces to the server
 * and the server receives them and recreates the object. This class adds the checksum to the beginning of each piece as the
 * identifier for which object it corresponds to.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/25/2014
 */

public class ComplexObject {

	/** Increments the complex object's id's. */
	private static int ID_INCREMENT = 0;
	
	/** The amount of splits to make. */
	private int splitAmount;
	
	/** The object's id. */
	private final int id;
	
	/** The data and checksum byte arrays. */
	private final byte[] data, checksum;
	
	/** Instance of the protocol being used. */
	private final IProtocol protocol;
	
	/** Array to hold all of the pieces. */
	private final ArrayList<ComplexPiece> pieces = new ArrayList<ComplexPiece>();
	
	/** Creates a new complex object and loads the pieces by splitting the data. 
	 * @param data The object's byte array. 
	 * @param checksum The object's checksum value.
	 * @param protocol Instance of the protocol.
	 */
	public ComplexObject(byte[] data, byte[] checksum, IProtocol protocol) {
		this(data, checksum, protocol, 3);
	}
	
	/** Creates a new complex object and loads the pieces by splitting the data. 
	 * @param data The object's byte array. 
	 * @param checksum The object's checksum value.
	 * @param protocol Instance of the protocol.
	 * @param splitAmount The amount of splits to make.
	 */
	public ComplexObject(byte[] data, byte[] checksum, IProtocol protocol, int splitAmount) {
		this.id = ID_INCREMENT++;
		this.data = data;	
		this.protocol = protocol;
		this.splitAmount = splitAmount;
		this.checksum = checksum;
		loadPieces();
		
	}
	
	/** Splits the object's byte array into pieces and gets them ready to be sent to over the stream. */
	private void loadPieces() {
		int bytesPerSend = data.length / splitAmount;
		if (bytesPerSend <= 10) { //Bytes per send needs to be > 10 so that the checksum can be extracted correctly. If not, change the split amount.
			splitAmount = data.length / 11;
			bytesPerSend = 11;
		}
		int extra = 0;
		int pieceAmount = splitAmount;
		
		if (bytesPerSend * splitAmount < data.length) {
			extra = data.length - (bytesPerSend * splitAmount);
			pieceAmount++;
		}
		for (int i = 0; i < splitAmount; i++) {
			byte[] splitData = copyArray(data, bytesPerSend, bytesPerSend * i);
			pieces.add(new ComplexPiece(i, pieceAmount, splitData, protocol, checksum));
		}
		if (extra > 0) {
			byte[] splitData = copyArray(data, extra, bytesPerSend * (splitAmount));
			pieces.add(new ComplexPiece(splitAmount, pieceAmount, splitData, protocol, checksum));
		}
	}
	
	/** Sends the object over TCP.
	 * @param tcpOut The TCP output stream.
	 */
	public void sendTcp(ObjectOutputStream tcpOut) {
		for (ComplexPiece piece : pieces)
			piece.sendTcp(tcpOut);
	}
	
	/** Sends the object over UDP.
	 * @param udpOut The UDP output stream.
	 * @param InetAddress The address to send it to.
	 * @param port The port to send it over.
	 */
	public void sendUdp(DatagramSocket udpOut, InetAddress address, int port) {
		for (ComplexPiece piece : pieces)
			protocol.executeThread(new ComplexUdpSendThread(piece, udpOut, address, port));
	}
	
	public void sendHttp(String url, String cookie, BufferedOutputStream out) {
		for (ComplexPiece piece : pieces) {
			piece.sendHttp(url, cookie, out);
		}
	}
	
	/** Copies part of one array to another.
	 * @param src The array to copy.
	 * @param sizeToCopy The amount of bytes being copied.
	 * @param startIndex The starting position in the source.
	 * @return The new byte array.
	 */
	private byte[] copyArray(byte[] src, int sizeToCopy, int startIndex) {
		byte[] ret = new byte[sizeToCopy];
		for (int i = 0; i < sizeToCopy; i++) {
			ret[i] = src[i + startIndex];
		}
		return ret;
	}
	

	/** Gets the checksum value from an object's byte array. It converts it to a String and makes sure that the length is 10.
	 * @param array The array to get the checksum of.
	 * @return The checksum.
	 */
	private byte[] getChecksum(byte[] array) {
		Checksum checksum = new CRC32();
		checksum.update(array, 0, array.length);
		String val = String.valueOf(checksum.getValue());
		
		while (val.length() < 10) {
			val += "0";
		}
		return val.getBytes();
	}
	
}
