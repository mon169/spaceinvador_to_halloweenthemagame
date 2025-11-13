package network;

import java.io.*;
import java.net.*;

public class ServerHandler implements Runnable {
    private final Socket socket;
    private final ObjectOutputStream out;

    public ServerHandler(Socket socket, ObjectOutputStream out) {
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Packet packet = (Packet) in.readObject();
                GameServer.broadcast(packet);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Client disconnected: " + socket.getInetAddress());
        }
    }
}