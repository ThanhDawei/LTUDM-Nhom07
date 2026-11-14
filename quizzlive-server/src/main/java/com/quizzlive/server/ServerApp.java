package com.quizzlive.server;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.gui.ServerMainFrame;
import com.quizzlive.server.network.SocketServer;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

public class ServerApp {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   QUIZZLIVE SERVER - NHÓM 07: TRANH TÀI KIẾN THỨC");
        System.out.println("=================================================");

        // 1. Khởi tạo Cơ sở dữ liệu (PostgreSQL / MySQL / H2 fallback + Schema + Seed Data)
        DatabaseManager.initialize();

        // Kiểm tra xem server có đang chạy trên môi trường không có màn hình (VPS Linux headless) hay không
        boolean isHeadless = GraphicsEnvironment.isHeadless();

        // 2. Khởi chạy Socket Server lắng nghe kết nối từ Client
        SocketServer server = new SocketServer(ServerConfig.getPort());
        Thread serverThread = new Thread(server::start, "Server-Socket-Thread");
        if (isHeadless) {
            // Trên VPS không có GUI, giữ tiến trình server chạy ngầm chính (non-daemon)
            serverThread.setDaemon(false);
        } else {
            serverThread.setDaemon(true);
        }
        serverThread.start();
        System.out.println("[Quizzlive Server] Socket Server is listening on port: " + ServerConfig.getPort());

        // 3. Khởi chạy Giao diện Quản trị Server (Chỉ mở khi có môi trường đồ họa)
        if (!isHeadless) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                ServerMainFrame mainFrame = new ServerMainFrame(server);
                mainFrame.setVisible(true);
                System.out.println("[Quizzlive Server] Admin GUI launched successfully.");
            });
        } else {
            System.out.println("[Quizzlive Server] Chạy ở chế độ Headless CLI (phù hợp VPS Linux).");
            System.out.println("[Quizzlive Server] Nhấn Ctrl+C để dừng server.");
        }
    }
}
