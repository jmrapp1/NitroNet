package com.jmr.wrapperx.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
import com.jmr.wrapper.common.utils.PacketUtils;
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
		String checksumSent = PacketUtils.getChecksumFromPacket(data);
		
		/** Return the object in bytes from the sent packet. */
		byte[] objectArray = PacketUtils.getObjectFromPacket(data);
		
		if (objectArray[0] == 99) { //Complex object
			PacketUtils.handleComplexPiece(checksumSent, objectArray, session);
		} else {
			try {
				/** Get the checksum value of the object array. */
				String checksumVal = PacketUtils.getChecksumOfObject(objectArray);
				
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
	
}
