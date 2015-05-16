package com.jmr.wrapper.server.threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.common.utils.PacketUtils;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.Server;

/**
 * Networking Library
 * ServerTcpReadThread.java
 * Purpose: Waits for new incoming TCP packets from the server. Decrypts them and passes them
 * to the listener if the checksum's match.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ServerTcpReadThread implements Runnable {

	/** Isntance of the connection. */
	private final Connection con;
	
	/** Instance of the server. */
	private final Server server;
	
	/** The input stream of the connection. */
	private ObjectInputStream in;
	
	/** Creates a new thread to wait for incoming packets.
	 * @param server Instance of the server.
	 * @param con Instance of the connection.
	 */
	public ServerTcpReadThread(Server server, Connection con) {
		this.con = con;
		this.server = server;
		try {
			in = new ObjectInputStream(con.getSocket().getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		byte[] data = null;
		while(con.getSocket() != null && !con.getSocket().isClosed() && in != null) {
			try {
				/** Get all data from the packet that was sent. */
				data = new byte[server.getConfig().PACKET_BUFFER_SIZE];
				try { 
					in.readFully(data);
				} catch (Exception e) { //Client disconnected and data wasn't finished sending
					ConnectionManager.getInstance().close(con);
					in.close();
					in = null;
					return;
				}
				/** Decrypt the data if the encryptor is set. */
				if (server.getEncryptionMethod() != null)
					data = server.getEncryptionMethod().decrypt(data);
				
				/** Get the checksum found before the packet was sent. */
				String checksumSent = PacketUtils.getChecksumFromPacket(data);
				
				/** Return the object in bytes from the sent packet. */
				byte[] objectArray = PacketUtils.getObjectFromPacket(data);
				if (objectArray != null) {
					
					if (objectArray[0] == 99) { //Complex object
						PacketUtils.handleComplexPiece(checksumSent, objectArray, con);
					} else {
					
						/** Get the checksum value of the object array. */
						String checksumVal = PacketUtils.getChecksumOfObject(objectArray);
						
						/** Get the object from the bytes. */
						ByteArrayInputStream objIn = new ByteArrayInputStream(objectArray);
						ObjectInputStream is = new ObjectInputStream(objIn);
						Object object = is.readObject();
						
						/** Check if the checksums are equal. If they aren't it means the packet was edited or didn't send completely. */
						if (checksumSent.equals(checksumVal)) {
							if (!(object instanceof String)) {
								server.executeThread(new ReceivedThread(server.getListener(), con, object));
							} else if (!((String) object).equalsIgnoreCase("ConnectedToServer") && !((String)object).equalsIgnoreCase("TestAlivePing")) {
								server.executeThread(new ReceivedThread(server.getListener(), con, object));
							}
						} else {
							con.addPacketLoss();
						}
						is.close();
						objIn.close();
					}
				}
			} catch (IOException | ClassNotFoundException e) { //disconnected
				e.printStackTrace();
				ConnectionManager.getInstance().close(con);
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				in = null;
			}
		}
	}	
	
}
