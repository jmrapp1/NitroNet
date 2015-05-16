package com.jmr.wrapper.server.threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.common.utils.PacketUtils;

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
			String checksumSent = PacketUtils.getChecksumFromPacket(data);
			
			/** Return the object in bytes from the sent packet. */
			byte[] objectArray = PacketUtils.getObjectFromPacket(data);
			
			if (objectArray[0] == 99) { //Complex object
				PacketUtils.handleComplexPiece(checksumSent, objectArray, con);
			} else {
				/** Get the checksum value of the object array. */
				String checksumVal = PacketUtils.getChecksumOfObject(objectArray);
				
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
		
}
