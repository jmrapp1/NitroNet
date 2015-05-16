package com.jmr.wrapper.client.threads;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.common.utils.PacketUtils;
import com.jmr.wrapper.server.threads.DisconnectedThread;
import com.jmr.wrapper.server.threads.ReceivedThread;

/**
 * Networking Library
 * ClientTcpReadThread.java
 * Purpose: Waits for new incoming TCP packets from the server. Decrypts them and passes them
 * to the listener if the checksum's match.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ClientTcpReadThread implements Runnable {

	/** Instance of the client object. */
	private final Client client;
	
	/** Instance of the connection to the server. */
	private final Connection serverConnection;
	
	/** Creates a new thread to wait for incoming TCP packets.
	 * @param client Instance of the client.
	 * @param serverConnection Instance of the server connection.
	 */
	public ClientTcpReadThread(Client client, Connection serverConnection) {
		this.client = client;
		this.serverConnection = serverConnection;
	}
	
	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(serverConnection.getSocket().getInputStream());
			while (!serverConnection.getSocket().isClosed() && in != null) {
				/** Get all data from the packet that was sent. */
				byte[] data = new byte[client.getConfig().PACKET_BUFFER_SIZE];
				try {
					in.readFully(data);
				} catch (Exception e) { //Connection lost to server and didnt finish sending data
					serverConnection.close();
					client.executeThread(new DisconnectedThread((SocketListener)client.getListener(), serverConnection));
					return; //kill thread
				}
				/** Decrypt the data if the encryptor is set. */
				if (client.getEncryptionMethod() != null)
					data = client.getEncryptionMethod().decrypt(data);

				/** Get the checksum found before the packet was sent. */
				String checksumSent = PacketUtils.getChecksumFromPacket(data);
				
				/** Return the object in bytes from the sent packet. */
				byte[] objectArray = PacketUtils.getObjectFromPacket(data);
				
				if (objectArray[0] == 99) { //Complex object
					PacketUtils.handleComplexPiece(checksumSent, objectArray, serverConnection);
				} else {
					
					/** Get the checksum value of the object array. */
					String checksumVal = PacketUtils.getChecksumOfObject(objectArray);
					
					/** Get the object from the bytes. */
					ByteArrayInputStream objIn = new ByteArrayInputStream(objectArray);
					ObjectInputStream is = new ObjectInputStream(objIn);
					Object object = is.readObject();
					
					/** Check if the checksums are equal. If they aren't it means the packet was edited or didn't send completely. */
					if (checksumSent.equals(checksumVal)) {
						if (object != null) {					
							client.executeThread(new ReceivedThread(client.getListener(), serverConnection, object));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			serverConnection.close();
			client.executeThread(new DisconnectedThread((SocketListener)client.getListener(), serverConnection));
		}
	}

}
