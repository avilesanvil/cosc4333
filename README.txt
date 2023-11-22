Richard Delforge, Cameron Devenport, Johnny Do

README.txt for Chat Application


Description

This chat application consists of two main Java classes: Server.java and Client.java. The server manages chat rooms and client connections, while the client connects to the server to send and receive messages.


Server (Server.java)

- Functionality: The server listens for incoming client connections, manages chat rooms, and broadcasts messages to clients within the same chat room.

- Key Features:
	Port scanning to find an available port.
	Handling multiple client connections using a thread pool.
	Supporting commands: JOIN, LEAVE, LISTROOMS, and message broadcasting.
	Timestamping messages and ensuring proper formatting.


Client (Client.java)

- Functionality: The client connects to the server, sends messages, and receives broadcasts from the server.

- Key Features:
	Connect to the server using a specified IP address and port.
	Send messages and commands to the server.
	Display messages received from the server with timestamps.


How to Run the Application

1. Start the Server:

	Compile the server: javac Server.java
	Run the server: java Server
	The server will start and listen on an available port.

2. Start the Client(s):

	Compile the client: javac Client.java
	Run the client: java Client
	Enter the server port number when prompted.
	Enter a name and use commands to join chat rooms and send messages.


Commands:

- JOIN <room_name>: Join a chat room. Replace <room_name> with the desired room's name.
- LEAVE: Leave the current chat room.
- LISTROOMS: List all available chat rooms and the number of users in each.
Sending a message: Type your message and press Enter to send it to the current chat room.


Troubleshooting:
Ensure the server is running before starting clients.
If unable to connect, check the server's port number and ensure it's not blocked by a firewall.
For any connection issues or exceptions, refer to the console output for error details.