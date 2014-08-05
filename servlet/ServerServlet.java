package servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;

import com.jmr.wrapper.common.complex.ComplexManager;
import com.jmr.wrapper.common.complex.ReceivedComplexPiece;
import com.jmr.wrapper.server.threads.NewConnectionThread;
import com.jmr.wrapper.server.threads.ReceivedThread;
import com.jmr.wrapperx.common.HttpSession;
import com.jmr.wrapperx.server.HttpServer;
import com.jmr.wrapperx.server.HttpSessionManager;

/**
 * Networking Library
 * ServerServlet.java
 * Purpose: Abstract class that is used when connecting to a servlet using the library. 
 * It will wait for incoming connections and packets, decrypt them, check and create 
 * cookies, and send the packets off to the set HttpListener.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

@WebServlet("/ServerServlet")
public abstract class ServerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/** Static instance of the Http Server. */
	protected static HttpServer server = HttpServer.getInstance();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/** Waits for incoming data from clients over Http.
	 * @param request The request information from the client.
	 * @param response The response being sent back to the client.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpEntity entity = new InputStreamEntity(request.getInputStream(), request.getContentLength());

		if (entity != null) {
			HttpSession session = null;
			if (request.getHeader("Cookie") != null) {
				String cookie = request.getHeader("Cookie");
				session = HttpSessionManager.getInstance().getSession(cookie);
			}
			if (session == null) {
				session = new HttpSession(request.getRemoteAddr(), request.getLocalName(), server);
				HttpSessionManager.getInstance().addSession(session);
				response.setHeader("Cookie", session.getCookie());
				session.setOutputStream(new BufferedOutputStream(response.getOutputStream()));
				new NewConnectionThread(server.getListener(), session).run();
			} else {
				response.setHeader("Cookie", session.getCookie());
				session.setOutputStream(new BufferedOutputStream(response.getOutputStream()));
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.writeTo(baos);
			byte[] data = baos.toByteArray();
			handlePacket(session, data, response);
		}
	}

	/** Decrypts incoming packets and sends them to the HttpListener.
	 * @param session The session it came from.
	 * @param data The data that was sent.
	 * @param response The response to be sent to the client.
	 */
	private void handlePacket(HttpSession session, byte[] data, HttpServletResponse response) {
		/** Decrypt the data if the encryptor is set. */
		if (server.getEncryptionMethod() != null)
			data = server.getEncryptionMethod().decrypt(data);
		
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
					if (object instanceof String && ((String) object).equalsIgnoreCase("ConnectionSetup")) {
						String val = (String) object;
						if (val.equalsIgnoreCase("ConnectionSetup"))
							session.send(new String("ConnectResponse"));
					} else {
						new ReceivedThread(server.getListener(), session, object).run(); //Cant create thread because response will send back before completed
					}
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

	/** Copies an array to a new array with the given size and start index.
	 * @param src The data being copied.
	 * @param arraySize The size of the new array.
	 * @param start The start index.
	 * @return The new array of data.
	 */
	private byte[] copyArray(byte[] src, int arraySize, int start) {
		byte[] ret = new byte[arraySize];
		for (int i = 0; i < arraySize; i++)
			ret[i] = src[i + start];
		return ret;
	}
	
	/** Gets the ID from a complex object. 
	 * @param data The data sent.
	 * @return The complex object ID.
	 */ 
	private int getIdFromComplex(byte[] data) {
		byte[] idArray = copyArray(data, 4, 1);
		int size = findSizeOfObject(idArray);
		idArray = copyArray(idArray, size, 0);
		String id = new String(idArray);
		return Integer.valueOf(id);
	}
	
	/** Gets the amount of pieces in the complex object.
	 * @param data The data sent.
	 * @return The amount.
	 */
	private int getPieceAmountFromComplex(byte[] data) {
		byte[] amountArray = copyArray(data, 4, 5);
		int size = findSizeOfObject(amountArray);
		amountArray = copyArray(amountArray, size, 0);
		String pieceAmount = new String(amountArray);
		return Integer.valueOf(pieceAmount);
	}
	
	/** Gets the object from a complex piece.
	 * @param data The data sent.
	 * @return
	 */
	private byte[] getObjectFromComplex(byte[] data) {
		return copyArray(data, data.length - 9, 9);
	}
	
	
}
