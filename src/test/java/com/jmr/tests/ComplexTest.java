package com.jmr.tests;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.exceptions.NNCantStartServer;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.server.ConnectionManager;
import com.jmr.wrapper.server.Server;

public class ComplexTest {


	public static void main(String[] args) {
		System.out.println("Complex Object Test.");
		System.out.println("Starting server on localhost port 1888.");
		try {
			Server server = new Server(1888, 1888);
			server.setListener(new ComplexTestListener());
			
			System.out.println("Waiting for clients.");
		} catch (NNCantStartServer e) {
			e.printStackTrace();
		}
	}
	
	
}

class ComplexTestListener implements SocketListener {

	ArrayList<Connection> clients = new ArrayList<Connection>();
	
	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received object: " + object);
		for (Connection c : clients) {
			if (c != con) {
				System.out.println("Sending received object to " + con.getAddress());
				c.sendComplexObjectTcp(object);
			}
		}
		System.out.println("Done handling received object.");
	}
	
	@Override
	public void connected(Connection con) {
		System.out.println("Client connected from " + con.getAddress() + ". Total: " + ConnectionManager.getInstance().getConnections().size());
		clients.add(con);
	}
	
	@Override
	public void disconnected(Connection con) {
		System.out.println("Client disconnected.");
		clients.remove(con);
	}
	
}

class ComplexTestClient {
	
	public static void main(String[] args) {
		Client client = new Client("localhost", 1888, 1888);
		client.setListener(new ComplexTestClientListener());
		client.connect();
	
		try {
			byte[] file = Files.readAllBytes(Paths.get("C:\\Users\\Jon\\Desktop\\segoeuib.ttf"));
			Holder holder = new Holder(file);
			if (client.isConnected()) {
				client.getServerConnection().sendComplexObjectTcp(holder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

class Holder implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public byte[] data;
	
	public Holder(byte[] data) {
		this.data = data;
	}
	
}

class ComplexTestClientListener implements SocketListener {
	
	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received object from server: " + object);
	}

	@Override
	public void connected(Connection con) {
		System.out.println("Connected to server.");
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Disconnected from server.");
	}
	
}
