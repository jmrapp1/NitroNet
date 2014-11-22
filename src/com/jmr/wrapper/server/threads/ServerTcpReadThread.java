package com.jmr.wrapper.server.threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.Server;

/**
 * Networking Library
 * ServerTcpReadThread.java
 * Purpose: Waits for new incoming TCP packets from the server. Decrypts them and passes them
 * to the listener if the checksum's match.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ServerTcpReadThread implements Runnable {

	/** Isntance of the connection. */
	private final Connection con;
	
	/** Instance of the server. */
	private final Server server;
	
	/** The input stream of the connection. */
	private ObjectInputStream in;
	
	/** Creates a new thread to wait for incoming packets.
	 * @param server Instance of the server.
	 * @param con Instance of the connection.
	 */
	public ServerTcpReadThread(Server server, Connection con) {
		this.con = con;
		this.server = server;
		try {
			in = new ObjectInputStream(con.getSocket().getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		byte[] data = null;
		while(!con.getSocket().isClosed() && in != null) {
			try {
				/** Get all data from the packet that was sent. */
				data = new byte[server.getConfig().PACKET_BUFFER_SIZE];
				in.readFully(data);
				
				/** Decrypt the data if the encryptor is set. */
				if (server.getEncryptionMethod() != null)
					data = server.getEncryptionMethod().decrypt(data);
				
				/** Get the checksum found before the packet was sent. */
				String checksumSent = getChecksumFromPacket(data);
				
				/** Return the object in bytes from the sent packet. */
				byte[] objectArray = getObjectFromPacket(data);
				if (objectArray != null) {
					
					if (objectArray[0] == 99) { //Complex object
						int id = getIdFromComplex(objectArray);
						int pieceAmount = getPieceAmountFromComplex(objectArray);
						objectArray = getObjectFromComplex(objectArray);
						ReceivedComplexPiece piece = new ReceivedComplexPiece(checksumSent, id, pieceAmount, objectArray);
						ComplexManager.getInstance().handlePiece(piece, con);
					} else {
					
						/** Get the checksum value of the object array. */
						String checksumVal = getChecksumOfObject(objectArray);
						
						/** Get the object from the bytes. */
						ByteArrayInputStream objIn = new ByteArrayInputStream(objectArray);
						ObjectInputStream is = new ObjectInputStream(objIn);
						Object object = is.readObject();
						
						/** Check if the checksums are equal. If they aren't it means the packet was edited or didn't send completely. */
						if (checksumSent.equals(checksumVal)) {
							if (!(object instanceof String)) {
								server.executeThread(new ReceivedThread(server.getListener(), con, object));
							} else if (!((String) object).equalsIgnoreCase("ConnectedToServer") && !((String)object).equalsIgnoreCase("TestAlivePing")) {
								server.executeThread(new ReceivedThread(server.getListener(), con, object));
							}
						} else {
							con.addPacketLoss();
						}
						is.close();
						objIn.close();
					}
				}
			} catch (IOException | ClassNotFoundException e) { //disconnected
				e.printStackTrace();
				server.executeThread(new DisconnectedThread((SocketListener)server.getListener(), con));
				ConnectionManager.getInstance().close(con);
				in = null;
			}
		}
	}
		
	/** Takes the bytes of an object's byte array, doesn't include the checksum bytes, finds
	 *  the size of the object, and returns the object in an array of bytes.
	 * @param data The object array sent from the packet.
	 * @return The object in a byte array.
	 */
	private byte[] getObjectFromPacket(byte[] data) {
		int index = 0;
		try {
			/** Find the size of the data. Gets rid of all extra null values. */
			index = findSizeOfObject(data);		
			
			//if (index > 10) {
				/** Create the byte array to store the object. Size is the size of the data array minus the size of the checksum. */
				byte[] objectArray = new byte[index - 10];
				
				/** Get the object and put the bytes into a separate array. */
				for (int i = 0; i < objectArray.length; i++)
					objectArray[i] = data[i + 10];
				
				if (objectArray[objectArray.length - 1] == -995) {
					byte[] temp = new byte[objectArray.length - 2];
					for (int i = 0; i < objectArray.length - 2; i++)
						temp[i] = objectArray[i];
					objectArray = temp;
				}
				//I love tetyana Martynyuk
				return objectArray;
			//} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Gets the first 10 bytes of the data, which is the checksum, and converts it to a string.
	 * @param data The packet sent from the server.
	 * @return The checksum in a string.
	 */
	private String getChecksumFromPacket(byte[] data) {
		/** Get the checksum value that was found before the packet was sent. */
		byte[] checksum = new byte[10];
		for (int i = 0; i < 10; i++)
			checksum[i] = data[i];
		return new String(checksum);
	}
	
	/** Finds the size of the object's byte array by removing any trailing zeroes.
	 * @param data The object's byte array.
	 * @return The shortened byte array.
	 */
	private int findSizeOfObject(byte[] data) {
		int count = 0;
		int index = -1;
		int checkIndex = 30;
		for (int i = 0; i < data.length; i++) {
			byte val = data[i];
			if (val == 0) {
				if (count >= data.length - checkIndex) {
					break;
				} else if (count == 0) {
					index = i;
					checkIndex = i;
				}
				count++;
			} else {
				count = 0;
			}
		}
		return index;
	}
	
	/** Takes the byte array of an object and gets the checksum from it.
	 * @param data The object's byte array.
	 * @return The checksum.
	 */
	private String getChecksumOfObject(byte[] data) {
		Checksum checksum = new CRC32();
		checksum.update(data, 0, data.length);
		String val = String.valueOf(checksum.getValue());
		while (val.length() < 10) {
			val += "0";
		}
		return val;
	}		

	/** Copies an array to a new array with the given size and start index.
	 * @param src The data being copied.
	 * @param arraySize The size of the new array.
	 * @param start The start index.
	 * @return The new array of data.
	 */
	private byte[] copyArray(byte[] src, int arraySize, int start) {
		System.out.println(arraySize);
		byte[] ret = new byte[arraySize];
		for (int i = 0; i < arraySize; i++)
			ret[i] = src[i + start];
		return ret;
	}
	
	/** Gets the ID from a complex object. 
	 * @param data The data sent.
	 * @return The complex object ID.
	 */ 
	private int getIdFromComplex(byte[] data) {
		byte[] idArray = copyArray(data, 4, 1);
		int size = findSizeOfObject(idArray);
		idArray = copyArray(idArray, size, 0);
		String id = new String(idArray);
		return Integer.valueOf(id);
	}
	
	/** Gets the amount of pieces in the complex object.
	 * @param data The data sent.
	 * @return The amount.
	 */
	private int getPieceAmountFromComplex(byte[] data) {
		byte[] amountArray = copyArray(data, 4, 5);
		int size = findSizeOfObject(amountArray);
		amountArray = copyArray(amountArray, size, 0);
		String pieceAmount = new String(amountArray);
		return Integer.valueOf(pieceAmount);
	}
	
	/** Gets the object from a complex piece.
	 * @param data The data sent.
	 * @return
	 */
	private byte[] getObjectFromComplex(byte[] data) {
		return copyArray(data, data.length - 9, 9);
	}
	
	
}
