package com.jmr.wrapper.common.complex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;

import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.utils.PacketUtils;

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
		Arrays.sort(pieces, pieceComparator);
		int dataSize = 0;
		for (ReceivedComplexPiece p : pieces) {
			dataSize += p.getDataSize();
		}
		byte[] data = new byte[dataSize];
		
		System.arraycopy(pieces[0].getData(), 14, data, 0, pieces[0].getDataSize()); //first 10 is the checksum, the next 4 is the size
		
		int inc = pieces[0].getDataSize();
		for (int i = 1; i < pieces.length; i++) {
			for (int j = 4; j < pieces[i].getData().length; j++) {
				data[inc + j - 4] = pieces[i].getData()[j];
			}
			inc += pieces[i].getDataSize();
		}
		try {
			if (protocol.getEncryptionMethod() != null)
				data = protocol.getEncryptionMethod().decrypt(data);

			/** Get the checksum value of the object array. */
			String checksumVal = PacketUtils.getChecksumOfObject(data);
			
			/** Get the object from the bytes. */
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			Object object = is.readObject();
			is.close();
			in.close();
			
			if (checksumVal.equalsIgnoreCase(checksum)) {
				return object;
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
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
	
	
	private static final Comparator<ReceivedComplexPiece> pieceComparator = new Comparator<ReceivedComplexPiece>() {

		@Override
		public int compare(ReceivedComplexPiece p1, ReceivedComplexPiece p2) {
			if (p1.getId() < p2.getId())
				return -1;
			else
				return 1;
		}
		
	};
	
}
