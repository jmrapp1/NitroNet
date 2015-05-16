package com.jmr.wrapper.common.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;

public class PacketUtils {

	/** Gets the ID from a complex object. 
	 * @param data The data sent.
	 * @return The complex object ID.
	 */
	public static int getIdFromComplex(byte[] data) {
		byte[] idArray = copyArray(data, 4, 1);
		int size = findSizeOfObject(idArray);
		if (size == 0)
			return 0;
		idArray = copyArray(idArray, size, 0);
		return intfromByteArray(idArray);
	}
	
	/** Gets the size of a complex object. 
	 * @param data The data sent.
	 * @return The complex object size.
	 */ 
	public static int getSizeFromComplex(byte[] data) {
		byte[] sizeArray = copyArray(data, 4, 1 + 4 + 4); //first byte is for the complex ID, next 4 is the ID, next 4 is the piece amount
		int size = findSizeOfObject(sizeArray);
		sizeArray = copyArray(sizeArray, size, 0);
		return intfromByteArray(sizeArray);
	}
	
	/** Gets the amount of pieces in the complex object.
	 * @param data The data sent.
	 * @return The amount.
	 */
	public static int getPieceAmountFromComplex(byte[] data) {
		byte[] amountArray = copyArray(data, 4, 1 + 4); //first byte is for the complex ID, next 4 is the ID
		int size = findSizeOfObject(amountArray);
		amountArray = copyArray(amountArray, size, 0);
		return intfromByteArray(amountArray);
	}
	
	/** Gets the object from a complex piece.
	 * @param data The data sent.
	 * @return
	 */
	public static byte[] getObjectFromComplex(byte[] data) {
		return copyArray(data, data.length - 9, 9);
	}
	
	/** Copies an array to a new array with the given size and start index.
	 * @param src The data being copied.
	 * @param arraySize The size of the new array.
	 * @param start The start index.
	 * @return The new array of data.
	 */
	public static byte[] copyArray(byte[] src, int arraySize, int start) {
		byte[] ret = new byte[arraySize];
		for (int i = 0; i < arraySize; i++)
			ret[i] = src[i + start];
		return ret;
	}
	
	/** Finds the size of the object's byte array by removing any trailing zeroes.
	 * @param data The object's byte array.
	 * @return The shortened byte array.
	 */
	public static int findSizeOfObject(byte[] data) {
		int i = data.length - 1;
	    while (i >= 0 && data[i] == 0)
	    {
	        --i;
	    }
	    return i + 1;
	}
	
	/** Takes the bytes of an object's byte array, doesn't include the checksum bytes, finds
	 *  the size of the object, and returns the object in an array of bytes.
	 * @param data The object array sent from the packet.
	 * @return The object in a byte array.
	 */
	public static byte[] getObjectFromPacket(byte[] data) {
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
	public static String getChecksumFromPacket(byte[] data) {
		/** Get the checksum value that was found before the packet was sent. */
		byte[] checksum = new byte[10];
		for (int i = 0; i < 10; i++)
			checksum[i] = data[i];
		return new String(checksum);
	}
	
	/** Takes the byte array of an object and gets the checksum from it.
	 * @param data The object's byte array.
	 * @return The checksum.
	 */
	public static String getChecksumOfObject(byte[] data) {
		Checksum checksum = new CRC32();
		checksum.update(data, 0, data.length);
		String val = String.valueOf(checksum.getValue());
		while (val.length() < 10) {
			val += "0";
		}
		return val;
	}
	
	/** Gets the byte array of the object, the checksum of the object, and combines them into
	 * an array of bytes. The first 10 bytes are the checksum and the remaining bytes are the
	 * object.
	 * @param stream The object's byte stream to get the byte array. 
	 * @param object The object being sent.
	 * @return The byte array with the size of it being Config.PACKET_BUFFER_SIZE
	 */
	public static byte[] getByteArray(IProtocol protocol, ByteArrayOutputStream stream) {
		byte[] array = stream.toByteArray();
		
		if (array[array.length - 1] == 0) {
			byte[] temp = new byte[array.length + 1];
			for (int i = 0; i < array.length; i++)
				temp[i] = array[i];
			temp[temp.length - 1] = (byte)-995;
			array = temp;
		}
		
		byte[] checksumBytes = getChecksumOfObject(array).getBytes();
		
		byte[] concat = new byte[protocol.getConfig().PACKET_BUFFER_SIZE];
		
		System.arraycopy(checksumBytes, 0, concat, 0, checksumBytes.length);
		System.arraycopy(array, 0, concat, checksumBytes.length, array.length);
		
		if (protocol.getEncryptionMethod() != null) {
			concat = protocol.getEncryptionMethod().encrypt(concat);
		}
		
		return concat;
	}
	
	/** Gets the byte array of the object, the checksum of the object, and combines them into
	 * an array of bytes. The first 10 bytes are the checksum and the remaining bytes are the
	 * object.
	 * @param stream The object's byte stream to get the byte array. 
	 * @param object The object being sent.
	 * @return The byte array with the size of it being the byte length of the object and checksum.
	 */
	public static byte[] getCompressedByteArray(IProtocol protocol, ByteArrayOutputStream stream) {
		byte[] array = stream.toByteArray();
		
		byte[] checksumBytes = getChecksumOfObject(array).getBytes();
		byte[] concat = new byte[checksumBytes.length + array.length];
		
		System.arraycopy(checksumBytes, 0, concat, 0, checksumBytes.length);
		System.arraycopy(array, 0, concat, checksumBytes.length, array.length);
		
		if (protocol.getEncryptionMethod() != null) {
			concat = protocol.getEncryptionMethod().encrypt(concat);
		}
		
		return concat;
	}
	
	/** Handles an incoming complex piece by getting its information and passing it on.
	 * 
	 * @param checksumSent The checksum to check against
	 * @param objectArray The array of data
	 * @param con The connection it came from
	 */
	public static void handleComplexPiece(String checksumSent, byte[] objectArray, IConnection con) {
		int id = getIdFromComplex(objectArray);
		int dataSize = getSizeFromComplex(objectArray);
		if (id == 0) 
			dataSize -= 10; //Removed 10 bytes which are used for the checksum in the front
		int pieceAmount = getPieceAmountFromComplex(objectArray);
		objectArray = getObjectFromComplex(objectArray);
		ReceivedComplexPiece piece = new ReceivedComplexPiece(checksumSent, id, pieceAmount, objectArray, dataSize);
		ComplexManager.getInstance().handlePiece(piece, con);
	}
	
	/** Converts and integer to a 4 byte long array.
	 * 
	 * @param value The integer
	 * @return 4 byte long array
	 */
	public static byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	/** Converts a 4 byte long array to an integer.
	 * 
	 * @param bytes The byte array
	 * @return The integer
	 */
	public static int intfromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
	
}