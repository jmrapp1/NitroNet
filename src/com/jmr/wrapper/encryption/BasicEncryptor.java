package com.jmr.wrapper.encryption;

/**
 * Networking Library
 * BasicEncryptor.java
 * Purpose: An example of a basic encryptor that adds a value of 1 to all bytes when encrypting
 * and removes a value of 1 from all bytes when decrypting. 
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class BasicEncryptor implements Encryptor {

	@Override
	public byte[] encrypt(byte[] data) {
		byte[] array = new byte[data.length];

		for (int i = 0; i < data.length; i++) {
			int value = data[i];
			value++;
			array[i] = (byte)value;
		}		
		return array;
	}

	@Override
	public byte[] decrypt(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] -= 1;
		}
		return data;
	}

	
}
