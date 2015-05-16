package com.jmr.wrapper.common.complex;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.utils.PacketUtils;
import com.jmr.wrapperx.client.HttpPostThread;
import com.jmr.wrapperx.server.HttpSendThread;

/**
 * Networking Library
 * ComplexPiece.java
 * Purpose: A piece of a object's byte array that corresponds to a complex object. This piece's data is sent over a socket
 * and recreated into an object later on.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/25/2014
 */

public class ComplexPiece {

	/** The ID. */
	private final int id;
	
	/** The amount of pieces in the complex object. */
	private final int pieceAmount;
	
	/** The byte array of the data and checksum. */
	private final byte[] data, checksum;
	
	/** Instance of the protocol. */
	private final IProtocol protocol;

	/** Creates a byte array holding the data, id, and checksum of the complex object.
	 * @param id The ID.
	 * @param pieceAmount The amount of pieces in the complex object.
	 * @param data The piece of the object's byte array.
	 * @param protocol Instance of the protocol. 
	 * @param checksum Object's checksum byte array.
	 */
	public ComplexPiece(int id, int pieceAmount, byte[] data, IProtocol protocol, byte[] checksum) {
		this.id = id;
		this.pieceAmount = pieceAmount;
		this.protocol = protocol;
		this.checksum = checksum;
		this.data = getByteArray(data);
	}
	
	/** Sends the piece over TCP.
	 * @param tcpOut The TCP output stream.
	 */
	public void sendTcp(ObjectOutputStream tcpOut) {
		try {
			synchronized(tcpOut) {
				tcpOut.write(data);
				tcpOut.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Sends the piece over UDP.
	 * @param udpOut The UDP output stream.
	 * @param address The address to send it to.
	 * @param port The port to send it over.
	 */
	public void sendUdp(DatagramSocket udpOut, InetAddress address, int port) {
		try {
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
			udpOut.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Sends the piece over HTTP.
	 * @param url The URL of the location of the servlet.
	 * @param cookie The session cookie.
	 * @param out The output stream of the response if on server side.
	 */
	public void sendHttp(String url, String cookie, BufferedOutputStream out) {
		if (protocol instanceof Client && url != null) {
			if (cookie == null)
				protocol.executeThread(new Thread(new HttpPostThread((Client)protocol, url, data)));
			else
				protocol.executeThread(new Thread(new HttpPostThread((Client)protocol, url, data, cookie)));
		}
	}
	
	/** Takes the id, converts it to four bytes and adds it in front of the bytes of data. It also makes the first index in the array
	 * equal to 99 because that is the key that will be used on the client/server side to determine whether or not it is part of a
	 * complex object.
	 * @param data The object's byte array.
	 * @return The combined array.
	 */
	private byte[] getByteArray(byte[] data) {
		byte[] indexArray = new byte[4]; //holds the ID amount
		copyArrayToArray(PacketUtils.intToByteArray(id), indexArray, 0); //Puts the ID into the array of 4 bytes
		
		byte[] pieceAmountArray = new byte[4];
		copyArrayToArray(PacketUtils.intToByteArray(pieceAmount), pieceAmountArray, 0); //Puts the ID into the array of 4 bytes
		
		byte[] dataSizeArray = new byte[4];
		copyArrayToArray(PacketUtils.intToByteArray(data.length), dataSizeArray, 0); //Puts the ID into the array of 4 bytes
		
		byte[] ret = new byte[data.length + 1 + indexArray.length + pieceAmountArray.length + dataSizeArray.length];
		
		ret[0] = 99; //1st Byte, Used to determine on the server/client side if the packet sent is part of a complex objects
		copyArrayToArray(indexArray, ret, 1); //2nd byte
		copyArrayToArray(pieceAmountArray, ret, 1 + indexArray.length);
		copyArrayToArray(dataSizeArray, ret, 1 + indexArray.length + pieceAmountArray.length);
		copyArrayToArray(data, ret, 1 + indexArray.length + pieceAmountArray.length + dataSizeArray.length);
		ret = addArrays(ret, checksum);
		return ret;
	}
	
	/** Takes the byte array of the object and id and puts the 10 bytes of the checksum in front of it.
	 * @param array The array to put the checksum in front of.
	 * @param checksumBytes The checksum value in bytes.
	 * @return The combined array.
	 */
	private byte[] addArrays(byte[] array, byte[] checksumBytes) {
		byte[] concat = new byte[protocol.getConfig().PACKET_BUFFER_SIZE];
		System.arraycopy(checksumBytes, 0, concat, 0, checksumBytes.length);
		System.arraycopy(array, 0, concat, checksumBytes.length, array.length);
		
		if (protocol.getEncryptionMethod() != null) {
			concat = protocol.getEncryptionMethod().encrypt(concat);
		}
		
		return concat;
	}
	
	/** Takes an array and copies it to the destination array.
	 * @param src The array to copy from.
	 * @param dest The array to copy to.
	 * @param startIndex The index in the array of "dest" to start at.
	 */
	private void copyArrayToArray(byte[] src, byte[] dest, int startIndex) {
		for (int i = 0; i < dest.length; i++) {
			if (i < src.length)
				dest[i + startIndex] = src[i];
			else
				break;
		}
	}
	
}
