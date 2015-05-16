package packets;

import java.io.Serializable;

public class ChatMessage implements Serializable {
	
	public String username, message;
	
	public ChatMessage(String username, String message) {
		this.username = username;
		this.message = message;
	}
	
}
