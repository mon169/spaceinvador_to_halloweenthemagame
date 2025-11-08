package network;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 9999;
    private static final List<ObjectOutputStream> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("🚀 Game Socket Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🎮 Client connected: " + socket.getInetAddress());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                clients.add(out);
                new Thread(new ServerHandler(socket, out)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(Packet packet) {
        System.out.println("📡 Broadcasting: " + packet); // 🔥 로그 추가
        for (ObjectOutputStream out : clients) {
            try {
                out.writeObject(packet);
                out.flush(); // 🔥 반드시 flush()
            } catch (IOException ignored) {}
        }
    }
}