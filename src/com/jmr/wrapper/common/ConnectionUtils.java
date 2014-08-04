package com.jmr.wrapper.common;

import java.io.ByteArrayOutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ConnectionUtils {

	/** Gets the byte array of the object, the checksum of the object, and combines them into
	 * an array of bytes. The first 10 bytes are the checksum and the remaining bytes are the
	 * object.
	 * @param stream The object's byte stream to get the byte array. 
	 * @param object The object being sent.
	 * @return The byte array with the size of it being Config.PACKET_BUFFER_SIZE
	 */
	public static byte[] getByteArray(NESocket neSocket, ByteArrayOutputStream stream, Object object) {
		byte[] array = stream.toByteArray();

		byte[] checksumBytes = getChecksum(array);
		
		byte[] concat = new byte[neSocket.getConfig().PACKET_BUFFER_SIZE];
		
		System.arraycopy(checksumBytes, 0, concat, 0, checksumBytes.length);
		System.arraycopy(array, 0, concat, checksumBytes.length, array.length);
		
		if (neSocket.getEncryptionMethod() != null) {
			concat = neSocket.getEncryptionMethod().encrypt(concat);
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
	public static byte[] getCompressedByteArray(NESocket neSocket, ByteArrayOutputStream stream, Object object) {
		byte[] array = stream.toByteArray();
		byte[] checksumBytes = getChecksum(array);
		
		byte[] concat = new byte[checksumBytes.length + array.length];
		
		System.arraycopy(checksumBytes, 0, concat, 0, checksumBytes.length);
		System.arraycopy(array, 0, concat, checksumBytes.length, array.length);
		
		if (neSocket.getEncryptionMethod() != null) {
			concat = neSocket.getEncryptionMethod().encrypt(concat);
		}
		
		return concat;
	}
	
	public static byte[] getChecksum(byte[] array) {
		Checksum checksum = new CRC32();
		checksum.update(array, 0, array.length);
		String val = String.valueOf(checksum.getValue());
		
		while (val.length() < 10) {
			val += "0";
		}
		
		return val.getBytes();
	}
	
}
