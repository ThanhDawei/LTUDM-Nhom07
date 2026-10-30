package com.quizzlive.client;

import com.quizzlive.client.config.ClientConfig;
import com.quizzlive.client.handler.ClientPacketHandler;
import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.ui.MainClientFrame;

import javax.swing.*;

/**
 * Điểm khởi chạy ứng dụng QuizzLive Client (Swing GUI)
 * Tuân thủ yêu cầu B.6: Client bắt buộc có GUI
 */
public class ClientApp {
    public static void main(String[] args) {
        // Cấu hình giao diện Look and Feel theo hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("=========================================");
            System.out.println("      QUIZZLIVE CLIENT - STARTING        ");
            System.out.println("=========================================");

            MainClientFrame frame = new MainClientFrame();

            // Đăng ký bộ điều phối gói tin mạng với SocketClient
            ClientPacketHandler packetHandler = new ClientPacketHandler(frame);
            SocketClient.getInstance().addListener(packetHandler);

            // Kết nối ngầm đến server qua TCP Socket
            new Thread(() -> {
                try {
                    System.out.println("[Client] Đang kết nối tới server " + ClientConfig.getHost() + ":" + ClientConfig.getPort() + "...");
                    SocketClient.getInstance().connect();
                } catch (Exception e) {
                    System.err.println("[Client] Không thể kết nối tới server: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Chưa thể kết nối tới Server (" + ClientConfig.getHost() + ":" + ClientConfig.getPort() + ")!\n" +
                                        "Vui lòng đảm bảo Server đã được bật trước khi đăng nhập.",
                                "Cảnh Báo Kết Nối",
                                JOptionPane.WARNING_MESSAGE
                        );
                    });
                }
            }).start();

            frame.setVisible(true);
        });
    }
}
