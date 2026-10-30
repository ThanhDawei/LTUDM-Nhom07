package com.quizzlive.client.network;

import com.quizzlive.client.config.ClientConfig;
import com.quizzlive.shared.protocol.Packet;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý kết nối Socket TCP phía Client
 * Chạy luồng nền (Daemon Thread) chuyên đọc gói tin từ Server
 */
public class SocketClient {
    private static SocketClient instance;

    private final String host;
    private final int port;
    private final boolean sslEnabled;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private volatile boolean connected = false;

    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();

    public SocketClient(String host, int port, boolean sslEnabled) {
        this.host = host;
        this.port = port;
        this.sslEnabled = sslEnabled;
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient(
                    ClientConfig.getHost(),
                    ClientConfig.getPort(),
                    ClientConfig.isSslEnabled()
            );
        }
        return instance;
    }

    public void addListener(NetworkListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public synchronized void connect() throws IOException {
        if (connected) return;

        SocketFactory factory = sslEnabled ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        socket = factory.createSocket(host, port);

        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        connected = true;

        System.out.println("[SocketClient] Đã kết nối thành công tới Server tại " + host + ":" + port + " (SSL: " + sslEnabled + ")");

        for (NetworkListener l : listeners) {
            l.onConnected();
        }

        // Khởi chạy luồng nền lắng nghe gói tin từ Server
        Thread listenThread = new Thread(this::listenLoop, "Client-Socket-Listener");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public synchronized void sendPacket(Packet packet) {
        if (!connected || output == null) {
            System.err.println("[SocketClient] Không thể gửi gói tin. Chưa kết nối tới Server.");
            return;
        }
        try {
            output.writeObject(packet);
            output.flush();
            System.out.println("[SocketClient] Đã gửi gói tin: " + packet.getOpCode());
        } catch (IOException e) {
            System.err.println("[SocketClient] Lỗi gửi gói tin: " + e.getMessage());
            disconnect();
        }
    }

    private void listenLoop() {
        try {
            while (connected) {
                Packet packet = (Packet) input.readObject();
                // Bắn sự kiện tới tất cả các listener đã đăng ký
                for (NetworkListener l : listeners) {
                    try {
                        l.onPacketReceived(packet);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SocketClient] Mất kết nối tới Server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public synchronized void disconnect() {
        if (!connected) return;
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}

        for (NetworkListener l : listeners) {
            l.onDisconnected();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
