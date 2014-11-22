package com.jmr.wrapper.server.threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;

public class UdpHandleThread implements Runnable {

	private final Connection con;
	private final IProtocol protocol;
	private final DatagramPacket readPacket;
	
	public UdpHandleThread(IProtocol protocol, Connection con, DatagramPacket readPacket) {
		this.con = con;
		this.protocol = protocol;
		this.readPacket = readPacket;
	}
	
	@Override
	public void run() {
		try {
			/** Get all data from the packet that was sent. */
			byte[] data = readPacket.getData();
			
			/** Decrypt the data if the encryptor is set. */
			if (protocol.getEncryptionMethod() != null)
				data = protocol.getEncryptionMethod().decrypt(data);
			
			/** Get the checksum found before the packet was sent. */
			String checksumSent = getChecksumFromPacket(data);
			
			/** Return the object in bytes from the sent packet. */
			byte[] objectArray = getObjectFromPacket(data);
			
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
				ByteArrayInputStream in = new ByteArrayInputStream(objectArray);
				ObjectInputStream is = new ObjectInputStream(in);
				Object object = is.readObject();
				
				/** Check if the checksums are equal. If they aren't it means the packet was edited or didn't send completely. */
				if (checksumSent.equals(checksumVal)) {
					if (object instanceof String && ((String) object).equalsIgnoreCase("SettingUdpPort")) {
						con.setUdpPort(readPacket.getPort());
					} else {
						protocol.executeThread(new ReceivedThread(protocol.getListener(), con, object));
					}
				} else {
					System.out.println("Lost: " + object + " Checksums: " + checksumSent + " - " + checksumVal);
					con.addPacketLoss();
				}
				
				is.close();
				in.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}


	/** Takes the bytes of an object's byte array, doesn't include the checksum bytes, finds
	 *  the size of the object, and returns the object in an array of bytes.
	 * @param data The object array sent from the packet.
	 * @return The object in a byte array.
	 */
	private byte[] getObjectFromPacket(byte[] data) {
		/** Find the size of the data. Gets rid of all extra null values. */
		int index = findSizeOfObject(data);		

		this.size = index;
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
		return objectArray;
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
		System.out.println("ArraySize: " + arraySize);
		byte[] ret = new byte[arraySize];
		for (int i = 0; i < arraySize; i++)
			ret[i] = src[i + start];
		return ret;
	}
	
	/** Gets the ID from a complex object. 
	 * @param data The data sent.
	 * @return The complex object ID.
	 */ 
	int size = -2;
	
	private int getIdFromComplex(byte[] data) {
		byte[] idArray = copyArray(data, 4, 1);
		int size = findSizeOfObject(idArray);
		idArray = copyArray(idArray, size, 0);
		String id = new String(idArray);
		id = removeSquares(id);

		return Integer.valueOf(id);
	}
	
	private String removeSquares(String str) {
		String newStr = "";
		for (int i = 0; i < str.length(); i++) {
			char c = getNumber(str.substring(i, i + 1));
			if (c != 0) {
				newStr += c;
			}
		}
		return newStr;
	}
	
	private char getNumber(String s) {
		try {
			Integer.parseInt(s);
			return s.charAt(0);
		} catch (Exception e) {
			return (char)0;
		}
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
		pieceAmount = removeSquares(pieceAmount);
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
