package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final Consumer<Packet> onReceive;
    private volatile boolean connected = true; // 🔥 연결 상태

    public GameClient(String host, int port, Consumer<Packet> onReceive) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.onReceive = onReceive;
        new Thread(new ClientHandler(socket, onReceive, this)).start();
        System.out.println("✅ Connected to server at " + host + ":" + port);
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public void disconnect() {
        connected = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
        System.out.println("🔌 Disconnected from server");
    }

    public void send(Packet packet) throws IOException {
        if (!isConnected()) return;
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            connected = false;
            throw new IOException("Socket closed or unreachable");
        }
    }
}