package server;

import packets.ChatMessage;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

public class ServerListener implements SocketListener {

	@Override
	public void connected(Connection con) {
		System.out.println("Client connected.");
		ConnectionManager.getInstance().addConnection(con);
		for (Connection c : ConnectionManager.getInstance().getConnections()) {
			c.sendTcp(new ChatMessage("", "Client has connected."));
		}
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Client disconnected.");
		ConnectionManager.getInstance().removeConnection(con);
		for (Connection c : ConnectionManager.getInstance().getConnections()) {
			c.sendTcp(new ChatMessage("", "Client has disconnected."));
		}
	}

	@Override
	public void received(Connection con, Object object) {
		if (object instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) object;
			for (Connection c : ConnectionManager.getInstance().getConnections()) {
				c.sendTcp(msg);
			}
		}
	}

}
