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
