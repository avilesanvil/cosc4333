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
import java.util.Date;
import java.util.Scanner;

public class Client {
    // Constants for server IP and date format for timestamps
    private static final String HOST = "localhost"; // Server IP address
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // Date format for timestamps

    public static void main(String[] args) {
        // Create a scanner for reading user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server port number: "); // Prompt the user to enter the server's port number
        int port = Integer.parseInt(scanner.nextLine()); // Read and parse the entered port number

        // Try-with-resources to manage socket and I/O streams
        try (Socket socket = new Socket(HOST, port); // Connect to the server at the specified port
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Output stream to send messages to the server
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Input stream to receive messages from the server
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) { // Input stream to read user's messages

            System.out.println("Connected to server on port " + port); // Confirm connection to the server

            // Start a new thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    // Continuously read messages from the server
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Display received messages without timestamps
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage()); // Error handling for I/O issues
                    e.printStackTrace();
                }
            }).start();

            // Main loop for sending messages
            String userInput;
            while ((userInput = stdIn.readLine()) != null) { // Read user input
                out.println(userInput); // Send the user input to the server

                // Check if the message is for joining a chat room or a normal message (not for leaving)
                if (userInput.startsWith("JOIN ") || !userInput.equals("LEAVE")) {
                    String time = sdf.format(new Date()); // Format the current time for the timestamp
                    // Display the user's message directly in the chat interface with a timestamp
                    System.out.println("[" + time + "] You: " + userInput);
                }
            }
        } catch (UnknownHostException ex) {
            System.err.println("Host unknown: " + ex.getMessage()); // Error handling for unknown host
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage()); // Error handling for I/O exceptions
            ex.printStackTrace();
        }
    }
}
