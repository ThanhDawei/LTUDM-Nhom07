package com.quizzlive.server.network;

import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.handler.PacketRouter;
import com.quizzlive.server.service.BattleManager;
import com.quizzlive.server.service.MatchmakingService;
import com.quizzlive.shared.protocol.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSession implements Runnable {
    private final Socket socket;
    private final SessionManager sessionManager;
    private final PacketRouter router;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Long userId;
    private String username;
    private boolean running = true;

    public ClientSession(Socket socket, SessionManager sessionManager, PacketRouter router) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Packet packet = (Packet) input.readObject();
                router.route(this, packet);
            }
        } catch (Exception e) {
            System.out.println("[Server] Session ended for: " + getRemoteAddress());
        } finally {
            cleanup();
        }
    }

    public synchronized void sendPacket(Packet packet) {
        try {
            if (output != null && !socket.isClosed()) {
                output.writeObject(packet);
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error sending packet to " + username + ": " + e.getMessage());
        }
    }

    private void cleanup() {
        running = false;
        if (userId != null) {
            MatchmakingService.getInstance().leaveQueue(userId);
            BattleManager.getInstance().handleUserDisconnected(userId);
            new UserDAO().updateOnlineStatus(userId, false);
        }
        sessionManager.removeSession(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public void close() {
        cleanup();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRemoteAddress() {
        return socket != null ? socket.getRemoteSocketAddress().toString() : "Unknown";
    }
}
