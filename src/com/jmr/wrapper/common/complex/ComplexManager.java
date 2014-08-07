package com.jmr.wrapper.common.complex;

import java.util.ArrayList;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.IConnection;
import com.jmr.wrapper.common.IProtocol;
import com.jmr.wrapper.server.threads.ReceivedThread;

/**
 * Networking Library
 * ComplexManager.java
 * Purpose: Singleton class that manages all complex objects. When a new complex object is received from a stream it is passed to here
 * and a new ReceivedComplexObject is created. As more pieces come in they are added to the correct objects and once all of the pieces
 * arrive the object is formed and passed to the event listener.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 7/25/2014
 */

public class ComplexManager {

	/** Singleton instance of the class. */
	private static final ComplexManager instance = new ComplexManager();
	
	/** Array to hold all complex objects that are being formed. */
	private final ArrayList<ReceivedComplexObject> complexObjects = new ArrayList<ReceivedComplexObject>();
	
	/** Instance of the protocol being used. */
	private IProtocol protocol;
	
	/** Default private constructor. */
	private ComplexManager() {
		
	}
	
	/** Sets the instance of the protocol being used.
	 * @param protocol The instance.
	 */
	public void setProtocol(IProtocol protocol) {
		this.protocol = protocol;
	}
	
	/** Handles incoming complex pieces and adds them to their correct complex object.
	 * @param piece The new piece.
	 * @param con The connection it came from.
	 */
	public void handlePiece(ReceivedComplexPiece piece, IConnection con) {
		synchronized(complexObjects) { //Synchronized because when it's UDP the ComplexObjects HashMap is edited at the same time.
			ReceivedComplexObject obj = getComplexObject(piece, con);
			if (obj == null) {
				obj = new ReceivedComplexObject(piece.getChecksum(), con, piece.getPieceSize(), protocol);
				complexObjects.add(obj);
			}
			obj.addPiece(piece);
			if (obj.isFormed()) {
				Object formed = obj.formObject();
				if (formed != null) {
					protocol.executeThread(new ReceivedThread(protocol.getListener(), con, formed));
				} else {
					System.out.println("Lost complex object.");
				}
				complexObjects.remove(obj);
			}
		}
	}
	
	/** Goes through the array of objects and finds the one that matches the piece's checksum and connection.
	 * @param piece The piece.
	 * @param con The connection it came from.
	 * @return The correct complex object.
	 */
	private ReceivedComplexObject getComplexObject(ReceivedComplexPiece piece, IConnection con) {
		for (int i = 0; i < complexObjects.size(); i++) {
			ReceivedComplexObject o = complexObjects.get(i);
			if (o.getConnection().equals(con) && o.getChecksum().equalsIgnoreCase(piece.getChecksum()))
				return o;
		}
		return null;
	}
	
	/** @return Singleton instance of the class. */
	public static ComplexManager getInstance() {
		return instance;
	}
	
}
