NitroNet
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

You will see the correct strings be outputed and you will also see a string being received called "TestAlivePing". This is a small packet sent to the client to test on the server side whether or not you are still connected. You can just ignore this packet. 

We have now setup the basis of a functional server and client.

Sending Packets
===============

We can now get into some interesting parts of the library and what it has to offer. The library seamlessly allows you to send packets over TCP and UDP using only one method for each. The library will also encrypt (we will go over this later) and ensure that the packet is fully received on the other side (I will go over what happens in the background at the end). Sending an object over to the server or client is very simple. We will first begin on the client side by sending a String to the server when connected.

```java
public class ClientListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received: " + object);
	}

	@Override
	public void connected(Connection con) {
		con.sendTcp("Hey how are you today?");
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Disconnected the server.");
	}
	
}

```

This will send the string "Hey how are you today?" when you first connect. Test it and then remove it after. We are going to send an object another way. Lets head to the ClientStarter class and send it from within there.

```java
public class ClientStarter {

	private final Client client;
	
	public ClientStarter() {
		client = new Client("localhost", 4395, 4395);
		client.setListener(new ClientListener());
		client.connect();
		if (client.isConnected()) {
			System.out.println("Connected to the server.");
			client.getServerConnection().sendTcp("Hey this was sent another way!");
		}
	}
	
	public static void main(String[] args) {
		new ClientStarter();
	}
	
}
```

The getter method "getServerConnection()" from Client returns the instance of the server connection object. From here you can send packets of data over to the server.

Lets see how we can now send information back to clients from the server. 

```java
public class ServerListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
		System.out.println("Received: " + object);
		con.sendUdp("I just got a packet from you!");
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

So now when a packet is received on the server side it will send back a string over UDP letting them know!

Sending Custom Objects
======================
You may be asking how you can send custom objects over TCP and UDP because strings arent all that helpful. When sending an object over a stream you need to serialize it to allow it to be broken down into bytes and be sent. For your own objects this is very simple, they just need to implement the Serializable interface.

```java
package packet;
import java.io.Serializable;

public class MyObject implements Serializable{
	
}
```

After that is done you can add any data you'd like into the object **as long as the data is serializeable as well**. What I mean by this is if you want to send a BufferedImage over the network and you add an instance variable into the object, it will not send and will throw an error because the BufferedImage object is not serializeable. In these cases the best option is to get the raw bytes from the object and add those into the object instead.

Before we start it is important to make sure these two things are true when sending an object over the network:

1. That the object on the client and server side is exactly the same. If the code of the object on one side is different from the code on the other side, it will not receive the object.

2. That the object being sent is in the same package on the server and clent side. So if you have two projects, one for the server and one for the client, and the object on the client side is in package "com.client.packets" and on the server side is in the package "com.server.packets", your program will throw an error because the packages are different. Simply make them reside in the same package like "com.network.packets".

If atleast one of these cases is not true your program will throw errors. 

Now lets continue working on our object and send data. We will add some instance variables to the MyObject class with a constructor to set the values of those variables.

```java
package packet;

import java.io.Serializable;

public class MyObject implements Serializable{

	public String message;
	
	public MyObject(String message) {
		this.message = message;
	}
	
}
```

This object will hold a message that is sent to either the server or the client. What we'll do on each side is go to the received method and add in code to extract the message from the object. First we'll start by going to the ServerListener class and modifying the received method.

```java
public class ServerListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
		if (object instanceof MyObject) {
			MyObject myObj = (MyObject) object;
			System.out.println(myObj.message);
		}
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

This code is pretty self explanatory but I will go through it to make sure you understand what is going on. When a new packet is received we first check to see if that packet is an instance of the MyObject class. If it is we cast the object to the MyObject class and print the message that was stored in the object. Now lets go to the client side and send the object over to the server.

```java
public class ClientStarter {

	private final Client client;
	
	public ClientStarter() {
		client = new Client("localhost", 4395, 4395);
		client.setListener(new ClientListener());
		client.connect();
		if (client.isConnected()) {
			System.out.println("Connected to the server.");
			client.getServerConnection().sendTcp(new MyObject("Hello from NitroNet!");
		}
	}
	
	public static void main(String[] args) {
		new ClientStarter();
	}
	
}
```

Now if you run the server and then the client the server will print out the message "Hello from NitroNet!".

Server and Client Configuration Settings
========================================
Both the Server and Client classes have configuration settings which, as of the most recent update, only have three options. The Server side has 2 options that you can modify:

1. Whether or not to ping connected clients to ensure they're connected. By default this is true and I recommend you leave it as that.

2. The time between each ping. By default it is 5000 milliseconds (5 seconds).

There is also 1 more option that both the Client and Server have that is important to understand:

1. The packet buffer size of the packets to be sent over TCP and UDP. By default the packet sizes are set to 2048 bytes. The highest amount that TCP and UDP protocol allows is 64,000 bytes. Say that you leave the size as 2048 bytes but you try to send an object that is 5000 bytes. It will throw an error because the object is larger than the buffer size. You may be wondering what to do if the byte size is over 64,000 and I will explain the solution in the next section.

To edit the configuration settings all you need to do is get the config instance from the Server or Client.

```java
client.getConfig().PACKET_BUFFER_SIZE = 32000;
```

```java
((ServerConfig)server.getConfig()).PACKET_BUFFER_SIZE = 32000;
((ServerConfig)server.getConfig()).PING_CLIENTS = false;
((ServerConfig)server.getConfig()).PING_SLEEP_TIME = 3000;
```

When changing the PACKET_BUFFER_SIZE you need to make sure that both buffer sizes are the same. Technically this isn't required but will prevent future issues. For example say you set the buffer size to 5000 on the Client and 2500 on the Server. Then say you sent an object from the Client that is 3000 bytes. It will send on the client side because it is under 5000 bytes but it will not receive on the server side because it is less than 3000 bytes. So I recommend to just be safe and set them to the same value.

Complex Objects/Packet Streaming
================================
Say that you're trying to send a very large object larger than 64,000 bytes. You can't just send it over the stream because it is too large. NitroNet implements packet streaming which it refers to as a 'Complex Object'. A Complex Object takes the bytes of an object, splits them into smaller parts, sends them over TCP or UDP, and then reforms the bytes and object on the receiving side. So if we have a 128,000 byte object we can split the object 10 times and send individual packets that are only 12,800 bytes long! I designed this system to be very dynamic and easy to use. All it requires is one method.

```java
client.getServerConnection().sendComplexObjectTcp(new MyObject("Hello from NitroNet!");
```

```java
client.getServerConnection().sendComplexObjectTcp(new MyObject("Hello from NitroNet!", 5);
```

These two snippets of code take the MyObject instance and go through the process of splitting it up. In the first case it splits the object into 3 parts, by default. In the second case it splits the object into the 5 parts as specified by the second parameter. 

Note that this process does have the drawback of taking a longer amount of time, a few milliseconds or so longer. 
