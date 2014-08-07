package com.jmr.wrapper.common.complex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.IProtocol;

/**
 * Networking Library
 * ReceivedComplexObject.java
 * Purpose: A complex object that was received over a stream. This contains the pieces of the object and methods to form the object
 * back together.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/25/2014
 */

public class ReceivedComplexObject {

	/** The connection it was sent from. */
	private final IConnection con;
	
	/** An array of all of the received pieces. */
	private final ReceivedComplexPiece[] pieces;
	
	/** The checksum value of the object. */
	private final String checksum;
	
	/** Instance of the protocol. */
	private final IProtocol protocol;

	/** The amount of pieces in the object. */
	private final int pieceSize;
	
	/** The current amount of pieces received. */
	private int index = 0;
	
	/** Creates a new complex object that was received over a stream.
	 * @param checksum The checksum of the object.
	 * @param con The connection the piece's came from.
	 * @param pieceSize The amount of pieces in the object.
	 * @param protocol Instance of the protocol. 
	 */
	public ReceivedComplexObject(String checksum, IConnection con, int pieceSize, IProtocol protocol) {
		this.checksum = checksum;
		this.con = con;
		this.pieceSize = pieceSize;
		this.protocol = protocol;
		pieces = new ReceivedComplexPiece[pieceSize];
	}
	
	/** Adds a piece to the array.
	 * @param piece The piece.
	 */
	public void addPiece(ReceivedComplexPiece piece) {
		pieces[index] = piece;
		index++;
	}
	
	/** Forms the object once all of the pieces have been received.
	 * @return The formed object.
	 */
	public Object formObject() {
		Arrays.sort(pieces);
		byte[] data = new byte[(pieces[0].getData().length * pieces[0].getPieceSize()) - 10];
		
		System.arraycopy(pieces[0].getData(), 10, data, 0, pieces[0].getData().length - 10);
		
		for (int i = 1; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].getData().length; j++) {
				data[(i * pieces[0].getData().length) - 10 + j] = pieces[i].getData()[j];
			}
		}
		try {

			int size = findSizeOfObject(data);
			if (size <= 0)
				size = data.length;
			byte[] objectArray = new byte[size];
			for (int i = 0; i < objectArray.length; i++)
				objectArray[i] = data[i];

			if (protocol.getEncryptionMethod() != null)
				data = protocol.getEncryptionMethod().decrypt(objectArray);

			/** Get the checksum value of the object array. */
			String checksumVal = getChecksumOfObject(objectArray);
			
			/** Get the object from the bytes. */
			ByteArrayInputStream in = new ByteArrayInputStream(objectArray);
			ObjectInputStream is = new ObjectInputStream(in);
			Object object = is.readObject();
			
			if (checksumVal.equalsIgnoreCase(checksum)) {
				return object;
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
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
	
	/** Finds the size of the object's byte array by removing any trailing zeroes.
	 * @param data The object's byte array.
	 * @return The shortened byte array.
	 */
	private int findSizeOfObject(byte[] data) {
		int count = 0;
		int index = -1;
		for (int i = 0; i < data.length; i++) {
			byte val = data[i];
			if (val == 0) {
				if (count >= 30) {
					break;
				} else if (count == 0) {
					index = i;
				}
				count++;
			} else {
				count = 0;
			}
		}
		return index;
	}
	
	/** @return The connection the object came from. */
	public IConnection getConnection() {
		return con;
	}
	
	/** @return The checksum of the object. */
	public String getChecksum() {
		return checksum;
	}
	
	/** @return Whether the object is ready to be formed. */
	public boolean isFormed() {
		return pieceSize == index;
	}
	
}
