package com.quizzlive.server.network;

import com.quizzlive.shared.protocol.Packet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionManager {
    private final CopyOnWriteArrayList<ClientSession> sessions = new CopyOnWriteArrayList<>();

    public void addSession(ClientSession session) {
        sessions.add(session);
        System.out.println("[SessionManager] Client connected: " + session.getRemoteAddress() + ". Active: " + sessions.size());
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
        System.out.println("[SessionManager] Client disconnected. Active: " + sessions.size());
    }

    public ClientSession getSessionByUserId(Long userId) {
        if (userId == null) return null;
        for (ClientSession s : sessions) {
            if (userId.equals(s.getUserId())) {
                return s;
            }
        }
        return null;
    }

    public void broadcast(Packet packet) {
        for (ClientSession session : sessions) {
            session.sendPacket(packet);
        }
    }

    public List<ClientSession> getOnlineSessions() {
        return sessions;
    }

    public int getActiveCount() {
        return sessions.size();
    }
}
