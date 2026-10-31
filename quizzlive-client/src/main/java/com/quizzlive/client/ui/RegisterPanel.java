package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng ký tài khoản và Xác thực OTP
 * Phụ trách Yêu cầu 1: "Đăng ký, đăng nhập, cập nhật thông tin như đề 01, có xác thực email bằng OTP"
 */
public class RegisterPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JButton btnRegister;
    private JButton btnBackToLogin;
    private JLabel lblStatus;

    private String pendingEmail;

    public RegisterPanel(MainClientFrame mainFrame) {
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
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(39, 174, 96));
        card.add(lblTitle, gbc);

        gbc.gridy++;
        JLabel lblSubtitle = new JLabel("Nhập email chính xác để nhận mã xác thực OTP (10 phút)", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);
        card.add(lblSubtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(18);
        card.add(txtUsername, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(18);
        card.add(txtPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Địa chỉ Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(18);
        card.add(txtEmail, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(18);
        card.add(txtFullName, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(Color.RED);
        card.add(lblStatus, gbc);

        gbc.gridy++;
        btnRegister = new JButton("Đăng Ký & Nhận OTP");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setBackground(new Color(39, 174, 96));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> handleRegister());
        card.add(btnRegister, gbc);

        gbc.gridy++;
        btnBackToLogin = new JButton("Đã có tài khoản? Đăng nhập");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBackToLogin.setBorderPainted(false);
        btnBackToLogin.setContentAreaFilled(false);
        btnBackToLogin.setForeground(new Color(41, 128, 185));
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOGIN));
        card.add(btnBackToLogin, gbc);

        add(card);
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText("Vui lòng điền đầy đủ tất cả các trường!");
            return;
        }

        if (username.length() < 3) {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText("Tên đăng nhập phải có tối thiểu 3 ký tự!");
            return;
        }

        if (password.length() < 6) {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText("Mật khẩu phải có tối thiểu 6 ký tự!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText("Định dạng email không hợp lệ!");
            return;
        }

        lblStatus.setForeground(new Color(41, 128, 185));
        lblStatus.setText("Đang gửi yêu cầu đăng ký...");
        btnRegister.setEnabled(false);
        this.pendingEmail = email;

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendRegister(username, password, email, fullName);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("Không thể kết nối đến máy chủ: " + e.getMessage());
                    btnRegister.setEnabled(true);
                });
            }
        }, "Client-Register-Worker").start();
    }

    /**
     * Callback được gọi bởi ClientPacketHandler khi server phản hồi REGISTER_RESP
     */
    public void onRegisterResponse(boolean success, String message) {
        btnRegister.setEnabled(true);
        if (success) {
            lblStatus.setText("");
            promptOtpDialog();
        } else {
            lblStatus.setForeground(Color.RED);
            lblStatus.setText(message != null ? message : "Đăng ký thất bại!");
        }
    }

    /**
     * Hiển thị popup nhập OTP kích hoạt tài khoản
     */
    private void promptOtpDialog() {
        String otp = JOptionPane.showInputDialog(
                this,
                "Mã OTP đã được gửi về email '" + pendingEmail + "'.\n(Xem log console Server nếu chạy cục bộ)\nVui lòng nhập mã OTP để kích hoạt:",
                "Xác thực Email (OTP - 10 phút)",
                JOptionPane.QUESTION_MESSAGE
        );

        if (otp != null && !otp.trim().isEmpty()) {
            lblStatus.setForeground(new Color(41, 128, 185));
            lblStatus.setText("Đang xác thực OTP...");

            new Thread(() -> {
                try {
                    if (!SocketClient.getInstance().isConnected()) {
                        SocketClient.getInstance().connect();
                    }
                    authService.sendVerifyOtp(pendingEmail, otp.trim());
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Lỗi mạng khi gửi OTP: " + e.getMessage());
                    });
                }
            }, "Client-VerifyOtp-Worker").start();
        }
    }

    /**
     * Callback được gọi bởi ClientPacketHandler khi server phản hồi VERIFY_OTP_RESP
     */
    public void onVerifyOtpResponse(boolean success, String message) {
        if (success) {
            JOptionPane.showMessageDialog(this, "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtPassword.setText("");
            mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
        } else {
            JOptionPane.showMessageDialog(this, "Xác thực OTP thất bại: " + message, "Lỗi OTP", JOptionPane.ERROR_MESSAGE);
            promptOtpDialog();
        }
    }
}
