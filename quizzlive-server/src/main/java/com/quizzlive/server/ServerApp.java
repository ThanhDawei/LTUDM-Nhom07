package com.quizzlive.server;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.gui.ServerMainFrame;
import com.quizzlive.server.network.SocketServer;

import javax.swing.*;

public class ServerApp {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   QUIZZLIVE SERVER - NHÓM 07: TRANH TÀI KIẾN THỨC");
        System.out.println("=================================================");

        // 1. Khởi tạo Cơ sở dữ liệu (MySQL / H2 fallback + Schema + Seed Data)
        DatabaseManager.initialize();

        // 2. Khởi chạy Socket Server lắng nghe kết nối từ Client
        SocketServer server = new SocketServer(ServerConfig.getPort());
        Thread serverThread = new Thread(server::start, "Server-Socket-Thread");
        serverThread.setDaemon(true);
        serverThread.start();

        // 3. Khởi chạy Giao diện Quản trị Server (Server GUI - Yêu cầu 12, 13, 15, 6)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            ServerMainFrame mainFrame = new ServerMainFrame(server);
            mainFrame.setVisible(true);
            System.out.println("[Quizzlive Server] Admin GUI launched successfully.");
        });
    }
}
