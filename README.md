NetworkingWrapper
=================

This library is used to simplify networking applications. It is multiplatform, supports UDP, TCP and HTTP, has the ability to implement encryption of packets, includes packet corruption handling, packet streaming, SQL support, and much more. This library will allow you to take full control of any application that needs to have some type of networking aspect implemented in it. 

Starting A Server
=================

The server is the heart of a networking application. It waits for incoming connections as well as incoming packets. Without the server there is nothing to communicate to clients and no connections being made. Starting a server using the library is very simple. The only information you need to specify is the TCP and UDP ports that you want to listen to.

```java
public class ServerStarter {

	private Server server;
	
	public ServerStarter() {
		try {
			server = new Server(4395, 4395);

			if (server.isConnected()) {
				System.out.println("Started server successfully.");
			}
		} catch (NECantStartServer e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ServerStarter();
	}
	
}
```
When starting the server we bound it to the port 4395. Both of the ports can be different if you choose, they don't have to be the same. Starting it also has the possibility to throw a NECantStartServer exception which is only thrown when a running server is already bounded to one of the ports you specified. When the program is run and no exceptions are thrown, it should print out "Started server successfully."


Starting A Client
================

When you want to connect to a server you need to have a client to connect to it. The client is what interacts with the server to send information or request information. We are going to start a client that connects to our server on the same ports.

```java
public class ClientStarter {

	private final Client client;
	
	public ClientStarter() {
		client = new Client("localhost", 4395, 4395);
		client.connect();
		if (client.isConnected()) {
			System.out.println("Connected to the server.");
		}
	}
	
	public static void main(String[] args) {
		new ClientStarter();
	}
	
}
```

We connect to the IP address "localhost" (which is our computer) on the TCP and UDP ports 4395. We then call the connect method and check to see if it connected to the server. 

Creating Socket Listeners
=========================

We have a server and client running and that's great but now we need to take control of both of them. We need a way to know when new connections are made, packets are received, and connecions disconnect. The library includes two listeners that do this for you. A SocketListener interface and a HttpListener inteface. The SocketListener is implemented when you want to add it to a TCP/UDP server or client. The HttpListener interface is implemented when you want to add it to a HTTP server or client. Because we are using sockets we will create a new listener for the server side.

```java
public class ServerListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received: " + object);
	}

	@Override
	public void connected(Connection con) {
		System.out.println("New client connected.");
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Client has disconnected.");
	}
	
}
```

The implemented methods are received, connected, and disconnected. Received is called when a new packet is received. Connected is called when a new connection is received. Disconnected is called when a connection leaves. The methods are very straightforward. So I have each method output a string when it is called. All we have to do now is attach the listener to the server.

```java
public class ServerStarter {

	private Server server;
	
	public ServerStarter() {
		try {
			server = new Server(4395, 4395);
			server.setListener(new ServerListener());
			if (server.isConnected()) {
				System.out.println("Started server successfully.");
			}
		} catch (NECantStartServer e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ServerStarter();
	}
	
}
```

When running the server and then the client you can test out the connected and disconnected method.

We will now create a SocketListener for the client-side the same way.

```java
public class ClientListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received: " + object);
	}

	@Override
	public void connected(Connection con) {
		System.out.println("Connected to the server.");
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Disconnected the server.");
	}
	
}
```

We then need to add it to the client.

```java
public class ClientStarter {

	private final Client client;
	
	public ClientStarter() {
		client = new Client("localhost", 4395, 4395);
		client.setListener(new ClientListener());
		client.connect();
		if (client.isConnected()) {
			System.out.println("Connected to the server.");
		}
	}
	
	public static void main(String[] args) {
		new ClientStarter();
	}
	
}
```

You will see the correct strings be outputed and you will also see a string being received called "TestAlivePing". This is a small packet sent to the client to test on the server side whether or not you are still connected. You may just ignore this packet. 

We have now setup the basis of a functional server and client.
