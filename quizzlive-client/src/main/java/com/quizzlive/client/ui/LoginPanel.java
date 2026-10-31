package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng nhập phía Client
 * Phụ trách Yêu cầu 1: Đăng nhập hệ thống, kiểm tra trạng thái kích hoạt OTP và khóa tài khoản
 */
public class LoginPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnGoRegister;
    private JLabel lblStatus;

    public LoginPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblTitle = new JLabel("QUIZZLIVE - TRANH TÀI KIẾN THỨC", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(41, 128, 185));
        card.add(lblTitle, gbc);

        gbc.gridy++;
        JLabel lblSubtitle = new JLabel("Đăng nhập để bắt đầu thi đấu trực tuyến", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);
        card.add(lblSubtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblUser = new JLabel("Tên đăng nhập / Email:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(18);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(txtUsername, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(18);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(txtPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(Color.RED);
        card.add(lblStatus, gbc);

        gbc.gridy++;
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        card.add(btnLogin, gbc);

        gbc.gridy++;
        btnGoRegister = new JButton("Chưa có tài khoản? Đăng ký ngay");
        btnGoRegister.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGoRegister.setBorderPainted(false);
        btnGoRegister.setContentAreaFilled(false);
        btnGoRegister.setForeground(new Color(41, 128, 185));
        btnGoRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoRegister.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_REGISTER));
        card.add(btnGoRegister, gbc);

        add(card);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        lblStatus.setForeground(new Color(41, 128, 185));
        lblStatus.setText("Đang xử lý đăng nhập...");
        btnLogin.setEnabled(false);

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendLogin(username, password);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("Không thể kết nối đến máy chủ: " + e.getMessage());
                    btnLogin.setEnabled(true);
                });
            }
        }, "Client-Login-Worker").start();
    }

    /**
     * Callback được gọi bởi ClientPacketHandler khi server phản hồi LOGIN_RESP
     */
    public void onLoginResponse(boolean success, String message) {
        btnLogin.setEnabled(true);
        if (success) {
            lblStatus.setText("");
            txtPassword.setText("");
            mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
        } else {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText(message != null ? message : "Đăng nhập thất bại!");
        }
    }
}
