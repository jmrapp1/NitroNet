package client;

import java.util.Scanner;

import packets.ChatMessage;

import com.jmr.wrapper.client.Client;

public class ClientStarter {

	private Client client;
	
	public ClientStarter() {
		client = new Client("localhost", 1337, 1337);
		client.setListener(new ClientListener());
		client.connect();
		
		Scanner in = new Scanner(System.in);
		
		if (client.isConnected()) {
			System.out.println("Enter a username: ");
			String username = in.nextLine();
			while(true) {
				String s = in.nextLine();
				ChatMessage msg = new ChatMessage(username, s);
				client.getServerConnection().sendTcp(msg);
			}
		}
	}
	
	public static void main(String[] args) {
		new ClientStarter();
	}

}
