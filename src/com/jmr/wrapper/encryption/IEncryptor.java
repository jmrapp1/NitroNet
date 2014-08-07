package com.jmr.wrapper.encryption;

/**
 * Networking Library
 * IEncryptor.java
 * Purpose: The interface for encryptors. Provides a method to encrypt and decrypt bytes of
 * data.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public interface IEncryptor {

	/** The method used to encrypt an object's byte array when being sent over different protocols.
	 * @param data The object's byte array.
	 * @return The encrypted byte array.
	 */
	byte[] encrypt(byte[] data);
	
	/** The method used to decrypt a byte array received over different protocols.
	 * @param data The byte array.
	 * @return The decrypted byte array.
	 */
	byte[] decrypt(byte[] data);
	
}
