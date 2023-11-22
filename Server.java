/*

	Richard Delforge, Cameron Devenport, Johnny Do
	Chat Room Project
	COSC 4333 - Distributed Systems
	Dr. Sun
	11/27/2023
	
*/

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    // Define default and maximum port numbers as constants
    private static final int DEFAULT_PORT = 9012;
    private static final int MAX_PORT = 65535;

    // Map to store chat rooms and their associated clients' PrintWriters
    private static Map<String, Set<PrintWriter>> chatRooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Find an available port to use, starting from the DEFAULT_PORT
        int port = findAvailablePort(DEFAULT_PORT);
        if (port == -1) {
            System.out.println("No available port found. Exiting.");
            return; // Exit if no available port is found
        }

        // Create a thread pool to manage client connections
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(port)) { // Try to open a server socket on the found port
            System.out.println("Server is listening on port " + port);

            while (true) { // Infinite loop to accept incoming client connections
                Socket clientSocket = serverSocket.accept(); // Accept an incoming connection
                pool.execute(new ClientHandler(clientSocket)); // Assign a new thread to handle the client
            }
        } catch (IOException ex) { // Catch any IOExceptions
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace(); 
        }
    }

    // Method to find an available port starting from a given port number
    private static int findAvailablePort(int startPort) {
        while (startPort <= MAX_PORT) { // Iterate through port numbers up to MAX_PORT
            try (ServerSocket serverSocket = new ServerSocket(startPort)) {
                return startPort; // Return the port number if the server socket is successfully opened
            } catch (IOException ignored) { // Catch IOException if the port is already in use
                startPort++; // Increment the port number and try the next one
            }
        }
        return -1; // Return -1 if no available port is found
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket clientSocket; 
        private PrintWriter out; // Output stream to send messages to the client
        private BufferedReader in; // Input stream to receive messages from the client
        private String currentRoom; // Current chat room of the client
        private String clientName; 

        // Constructor for ClientHandler
        public ClientHandler(Socket socket) {
            this.clientSocket = socket; // Assign the client's socket
        }

        // The run method of the thread handling the client
        public void run() {
            try {
                // Initialize the output and input streams
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Send a message to the client to enter their name
                out.println("Enter your name:");
                clientName = in.readLine(); // Read the client's name
                // Send a welcome message and instructions
                out.println("Welcome " + clientName + "! You can join a room with JOIN <room_name>, leave with LEAVE, list existing chatrooms with LISTROOMS, or send messages.");

                String inputLine; // Variable to store input from the client
                // Read messages from the client
                while ((inputLine = in.readLine()) != null) {
                    // Handle different commands: JOIN, LEAVE, LISTROOMS, or general messages
                    if (inputLine.startsWith("JOIN ")) {
                        joinChatRoom(inputLine.substring(5)); // Handle joining a room
                    } else if ("LEAVE".equals(inputLine)) {
                        leaveChatRoom(); // Handle leaving a room
                    } else if ("LISTROOMS".equals(inputLine)) {
                        listChatRooms(); // Handle listing available chat rooms
                    } else {
                        // Handle sending a general message
                        sendMessageToChatRoom(clientName + ": " + inputLine, this.out); // Pass the sender's PrintWriter
                    }
                }
            } catch (IOException ex) { // Catch IOExceptions
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace(); // Print the stack trace for debugging
            } finally {
                leaveChatRoom(); // Ensure the client leaves the chat room
                closeResources(); // Close all resources associated with the client
            }
        }

        // Method to handle a client joining a chat room
        private void joinChatRoom(String roomName) {
            leaveChatRoom(); // Ensure the client leaves any current room
            // Add the client to the new chat room
            chatRooms.computeIfAbsent(roomName, k -> new HashSet<>()).add(out);
            currentRoom = roomName; // Update the current room for the client
            out.println("Entered room: " + roomName); // Notify the client
            System.out.println(clientName + " has entered chat room: " + roomName); // Log on server
        }

        // Method to handle a client leaving a chat room
        private void leaveChatRoom() {
            // Check if the client is in a room
            if (currentRoom != null && chatRooms.containsKey(currentRoom)) {
                chatRooms.get(currentRoom).remove(out); // Remove the client from the room
                // If the room is empty, remove it from the chatRooms map
                if (chatRooms.get(currentRoom).isEmpty()) {
                    chatRooms.remove(currentRoom);
                }
                out.println("Left room: " + currentRoom); // Notify the client
                System.out.println(clientName + " has left chat room: " + currentRoom); // Log on server
                currentRoom = null; // Reset the current room
            }
        }

        // Method to list all available chat rooms to the client
        private void listChatRooms() {
            out.println("Available chat rooms:");
            // Iterate through the chatRooms map
            for (Map.Entry<String, Set<PrintWriter>> entry : chatRooms.entrySet()) {
                // Send each room and its user count to the client
                out.println(" - " + entry.getKey() + " (" + entry.getValue().size() + " users)");
            }
        }

        // Method to send a message to all clients in the current chat room, excluding the sender
        private void sendMessageToChatRoom(String message, PrintWriter senderOut) {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date()); // Format the current time
            String formattedMessage = "\n" + "[" + time + "] " + message.trim(); // Trim to remove extra spaces
            // Check if the room exists and is not empty
            if (currentRoom != null && chatRooms.containsKey(currentRoom)) {
                // Iterate over all clients in the room
                for (PrintWriter writer : chatRooms.get(currentRoom)) {
                    // Exclude the sender from receiving the message
                    if (writer != senderOut) { 
                        writer.println(formattedMessage); // Send the formatted message
                    }
                }
            }
        }

        // Method to close all resources associated with the client
        private void closeResources() {
            try {
                if (out != null) out.close(); // Close PrintWriter
                if (in != null) in.close(); // Close BufferedReader
                if (clientSocket != null) clientSocket.close(); // Close client socket
            } catch (IOException ex) {
                ex.printStackTrace(); // Print stack trace in case of exception
            }
        }
    }
}

