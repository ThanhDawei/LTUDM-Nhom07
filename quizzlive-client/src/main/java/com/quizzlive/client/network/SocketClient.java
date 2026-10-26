package com.quizzlive.client.network;

import com.quizzlive.shared.protocol.Packet;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private final String host;
    private final int port;
    private final boolean sslEnabled;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected = false;

    public SocketClient(String host, int port, boolean sslEnabled) {
        this.host = host;
        this.port = port;
        this.sslEnabled = sslEnabled;
    }

    public void connect() throws IOException {
        SocketFactory factory = sslEnabled ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        socket = factory.createSocket(host, port);

        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        connected = true;

        System.out.println("[Client] Connected to server at " + host + ":" + port + " (SSL: " + sslEnabled + ")");

        Thread listenThread = new Thread(this::listenLoop);
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public synchronized void sendPacket(Packet packet) {
        if (!connected) {
            System.err.println("[Client] Cannot send packet. Not connected.");
            return;
        }
        try {
            output.writeObject(packet);
            output.flush();
            System.out.println("[Client] Sent packet: " + packet.getOpCode());
        } catch (IOException e) {
            System.err.println("[Client] Error sending packet: " + e.getMessage());
        }
    }

    private void listenLoop() {
        try {
            while (connected) {
                Packet packet = (Packet) input.readObject();
                System.out.println("[Client] Received from Server -> OpCode: " + packet.getOpCode() + " Payload: " + packet.getPayloadJson());
            }
        } catch (Exception e) {
            System.out.println("[Client] Connection to server lost.");
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
