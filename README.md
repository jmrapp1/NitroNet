NetworkingWrapper
=================

This library is used to simplify networking applications. It is multiplatform, supports UDP, TCP and HTTP, has the ability to implement encryption of packets, includes packet corruption handling, packet streaming, SQL support, and much more. This library will allow you to take full control of any application that needs to have some type of networking aspect implemented in it. 

Starting A Server
=================

The server is the heart of a networking application. It waits for incoming connections as well as incoming packets. Without the server there is nothing to communicate to clients and no connections being made. Starting a server using the library is very simple. The only information you need to specify is the TCP and UDP ports that you want to listen to.

```
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
When starting the server we bound it to the port 4395. Both of the ports can be different if you choose, they don't have to be the same. Starting it also has the possibility to throw a NECantStartServer exception which is only thrown when a running server is already bounded to one of the ports you specified. 


Starting A Client
================

When you want to connect to a server you need to have a client to connect to it. 
