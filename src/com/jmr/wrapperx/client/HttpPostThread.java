package com.jmr.wrapperx.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.server.threads.NewConnectionThread;
import com.jmr.wrapper.server.threads.ReceivedThread;
import com.jmr.wrapperx.common.HttpListener;
import com.jmr.wrapperx.common.HttpSession;

/**
 * Networking Library
 * HttpPostThread.java
 * Purpose: Sends a post request to the servlet and handles the response if one is sent.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpPostThread implements Runnable {
	
	/** Data being sent to the servlet. */
	private final byte[] data;
	
	/** Used to send post and get requests over to a servlet. */
	private final HttpClient httpClient;
	
	/** Instance of the client. */
	private final Client client;

	/** URL location of the servlet. */
	private final String url;
	
	/** The cookie, if any, of the client's session. */
	private final String cookie;
	
	/** Sends a byte array of data over to a servlet without cookies. 
	 * @param client Instance of the client.
	 * @param url The URL location of the servlet.
	 * @param data The byte array to send.
	 */
	public HttpPostThread(Client client, String url, byte[] data) {
		this.data = data;
		this.httpClient = new DefaultHttpClient();
		this.client = client;
		this.url = url;
		cookie = null;
	}
	
	/** Sends a byte array of data over to a servlet with the set cookies. 
	 * @param client Instance of the client.
	 * @param url The URL location of the servlet.
	 * @param data The byte array to send.
	 * @param cookie The cookies of the client's session.
	 */
	public HttpPostThread(Client client, String url, byte[] data, String cookie) {
		this.data = data;
		this.httpClient = new DefaultHttpClient();
		this.client = client;
		this.url = url;
		this.cookie = cookie;
	}
	
	@Override
	public void run() {
		try {
			HttpContext context = null;
			HttpPost post = new HttpPost(url);
			post.setEntity(new ByteArrayEntity(data));

			if (cookie != null && !cookie.equalsIgnoreCase("")) {
				post.addHeader("Cookie", cookie);
			}
			
			HttpResponse res = httpClient.execute(post, context);
			HttpEntity entity = res.getEntity();

			if (entity != null) {
				for (Header header : res.getAllHeaders()) {
					if (header.getName().equalsIgnoreCase("Cookie")) {
						String cookie = header.getValue();
						
						if (!client.getHttpConnection().getSession().getCookie().equalsIgnoreCase(cookie)) {
							client.getHttpConnection().setSession(new HttpSession(cookie, client, url));
							new NewConnectionThread(client.getListener(), client.getHttpConnection().getSession()).run();
						}
						break;
					}
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				entity.writeTo(baos);
				byte[] data = baos.toByteArray();
				if (data != null && data.length > 0) 
					handlePacket(client.getHttpConnection().getSession(), data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Decrypts a byte array and handles any complex objects. Sends the object to the 
	 * listener if it is formed.
	 *
	 * @param session The client's session.
	 * @param data The data to send over.
	 */
	private void handlePacket(HttpSession session, byte[] data) {
		/** Decrypt the data if the encryptor is set. */
		if (client.getEncryptionMethod() != null)
			data = client.getEncryptionMethod().decrypt(data);
		
		/** Get the checksum found before the packet was sent. */
		String checksumSent = getChecksumFromPacket(data);
		
		/** Return the object in bytes from the sent packet. */
		byte[] objectArray = getObjectFromPacket(data);
		
		if (objectArray[0] == 99) { //Complex object
			int id = getIdFromComplex(objectArray);
			int pieceAmount = getPieceAmountFromComplex(objectArray);
			objectArray = getObjectFromComplex(objectArray);
			ReceivedComplexPiece piece = new ReceivedComplexPiece(checksumSent, id, pieceAmount, objectArray);
			ComplexManager.getInstance().handlePiece(piece, session);
		} else {
			try {
				/** Get the checksum value of the object array. */
				String checksumVal = getChecksumOfObject(objectArray);
				
				/** Get the object from the bytes. */
				ByteArrayInputStream objIn = new ByteArrayInputStream(objectArray);
				
				ObjectInputStream is = new ObjectInputStream(objIn);
				
				Object object = is.readObject();
				
				/** Check if the checksums are equal. If they aren't it means the packet was edited or didn't send completely. */
				if (checksumSent.equals(checksumVal)) {
					if (!(object instanceof String && ((String) object).equalsIgnoreCase("ConnectResponse"))) {
						client.executeThread(new ReceivedThread((HttpListener)client.getListener(), session, object));
					}
				} else {
					client.getServerConnection().addPacketLoss();
				}
				is.close();
				objIn.close();
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
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
		
		/** Create the byte array to store the object. Size is the size of the data array minus the size of the checksum. */
		byte[] objectArray = new byte[index - 10];
		
		/** Get the object and put the bytes into a separate array. */
		for (int i = 0; i < objectArray.length; i++)
			objectArray[i] = data[i + 10];
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
		for (int i = 0; i < data.length; i++) {
			byte val = data[i];
			if (val == 0) {
				if (count >= 20) {
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

	private byte[] copyArray(byte[] src, int arraySize, int start) {
		byte[] ret = new byte[arraySize];
		for (int i = 0; i < arraySize; i++)
			ret[i] = src[i + start];
		return ret;
	}
	
	private int getIdFromComplex(byte[] data) {
		byte[] idArray = copyArray(data, 4, 1);
		int size = findSizeOfObject(idArray);
		idArray = copyArray(idArray, size, 0);
		String id = new String(idArray);
		return Integer.valueOf(id);
	}
	
	private int getPieceAmountFromComplex(byte[] data) {
		byte[] amountArray = copyArray(data, 4, 5);
		int size = findSizeOfObject(amountArray);
		amountArray = copyArray(amountArray, size, 0);
		String pieceAmount = new String(amountArray);
		return Integer.valueOf(pieceAmount);
	}
	
	private byte[] getObjectFromComplex(byte[] data) {
		return copyArray(data, data.length - 9, 9);
	}
	
}
