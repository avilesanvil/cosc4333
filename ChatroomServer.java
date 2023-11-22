
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatroomServer {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ChatRoom> chatRooms;

    public ChatroomServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        chatRooms = new ConcurrentHashMap<>();
        System.out.println("Chat Server is listening on port " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void handleJoinRequest(String chatRoomName, ClientHandler clientHandler) {
        chatRooms.computeIfAbsent(chatRoomName, k -> new ChatRoom()).addClient(clientHandler);
    }

    public synchronized void handleLeaveRequest(String chatRoomName, ClientHandler clientHandler) {
        ChatRoom chatRoom = chatRooms.get(chatRoomName);
        if (chatRoom != null) {
            chatRoom.removeClient(clientHandler);
            if (chatRoom.isEmpty()) {
                chatRooms.remove(chatRoomName);
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345; // Example port number
        try {
            ChatroomServer server = new ChatroomServer(port);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting the chat server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ChatRoom {
    private ConcurrentLinkedQueue<ClientHandler> clients;

    public ChatRoom() {
        clients = new ConcurrentLinkedQueue<>();
    }

    public void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private ChatroomServer server;
    private PrintWriter writer;

    public ClientHandler(Socket socket, ChatroomServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            String chatRoomName = reader.readLine(); // Simplified for example
            server.handleJoinRequest(chatRoomName, this);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                // Handle messages
            }

            server.handleLeaveRequest(chatRoomName, this);

        } catch (IOException ex) {
            System.out.println("Error in ClientHandler: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
