
import java.io.*;
import java.net.*;

public class ChatroomClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatroomClient(String address, int port) throws IOException {
        socket = new Socket(address, port);
        InputStream input = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
    }

    public void joinChatRoom(String chatRoomName) {
        writer.println(chatRoomName); // Sending JOIN request with chat room name
    }

    public void sendMessage(String message) {
        writer.println(message); // Sending a message to the chat room
    }

    public void leaveChatRoom() {
        writer.println("LEAVE"); // Sending LEAVE request
    }

    public void listenForMessages() {
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        String address = "127.0.0.1"; // Example server address
        int port = 12345; // Example server port

        try {
            ChatroomClient client = new ChatroomClient(address, port);
            client.joinChatRoom("exampleRoom"); // Joining a chat room
            client.listenForMessages();

            // Example usage
            client.sendMessage("Hello from client!");
            // client.leaveChatRoom();

        } catch (IOException e) {
            System.out.println("Error connecting to the chat server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
