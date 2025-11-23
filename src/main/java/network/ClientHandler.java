package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Consumer<Packet> onReceive;
    private final GameClient parent; // 🔥 추가

    public ClientHandler(Socket socket, Consumer<Packet> onReceive, GameClient parent) {
        this.socket = socket;
        this.onReceive = onReceive;
        this.parent = parent;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Packet packet = (Packet) in.readObject();
                onReceive.accept(packet);
            }
        } catch (Exception e) {
            System.out.println("❌ Disconnected from server: " + e.getMessage());
            parent.disconnect(); // 🔥 연결 종료 상태 반영
        }
    }
}