package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.util.BrutalUI;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng nhập phía Client - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế Card trắng viền đen 2px, bóng cứng 5px, tương phản cao, badge nổi bật
 * Phụ trách Yêu cầu 1: Đăng nhập hệ thống, kiểm tra trạng thái kích hoạt OTP và khóa tài khoản
 */
public class LoginPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private BrutalUI.BrutalButton btnLogin;
    private BrutalUI.BrutalButton btnGoRegister;
    private JLabel lblStatus;

    public LoginPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(BrutalUI.COLOR_BG);

        // Card chính phong cách Neo-Brutalist
        BrutalUI.CardPanel card = new BrutalUI.CardPanel(16, 5);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // 1. Badge Neo-Brutalist
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeRow.setOpaque(false);
        JLabel badge = BrutalUI.createBadge("TRANH TÀI KIẾN THỨC", new Color(254, 240, 138), BrutalUI.COLOR_BLACK);
        badgeRow.add(badge);
        card.add(badgeRow, gbc);

        // 2. Logo "Q" + Title QUIZZLIVE
        gbc.gridy++;
        gbc.insets = new Insets(10, 6, 4, 6);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        headerPanel.setOpaque(false);

        JPanel logoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Q";
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1);
                g2.dispose();
            }
        };
        logoBox.setPreferredSize(new Dimension(42, 42));
        headerPanel.add(logoBox);

        JLabel lblTitle = new JLabel("QUIZZLIVE");
        lblTitle.setFont(BrutalUI.FONT_TITLE_XL);
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);
        headerPanel.add(lblTitle);
        card.add(headerPanel, gbc);

        // 3. Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(0, 6, 16, 6);
        JLabel lblSubtitle = new JLabel("Đăng nhập để bắt đầu thi đấu trực tuyến nhiều người chơi", SwingConstants.CENTER);
        lblSubtitle.setFont(BrutalUI.FONT_SMALL);
        lblSubtitle.setForeground(BrutalUI.COLOR_MUTED);
        card.add(lblSubtitle, gbc);

        // 4. Ô nhập Tên đăng nhập
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        JLabel lblUser = new JLabel("Tên đăng nhập / Email:");
        lblUser.setFont(BrutalUI.FONT_BODY_BOLD);
        lblUser.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(18);
        txtUsername.setFont(BrutalUI.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(240, 38));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtUsername.addActionListener(e -> handleLogin());
        card.add(txtUsername, gbc);

        // 5. Ô nhập Mật khẩu
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(BrutalUI.FONT_BODY_BOLD);
        lblPass.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(18);
        txtPassword.setFont(BrutalUI.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(240, 38));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtPassword.addActionListener(e -> handleLogin());
        card.add(txtPassword, gbc);

        // 6. Status Label
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 6, 8, 6);
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(BrutalUI.FONT_SMALL_BOLD);
        lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
        card.add(lblStatus, gbc);

        // 7. Nút Đăng nhập
        gbc.gridy++;
        gbc.insets = new Insets(4, 6, 8, 6);
        btnLogin = new BrutalUI.BrutalButton("ĐĂNG NHẬP", BrutalUI.BrutalButton.Style.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(360, 44));
        btnLogin.setFont(BrutalUI.FONT_BODY_BOLD);
        btnLogin.addActionListener(e -> handleLogin());
        card.add(btnLogin, gbc);

        // 8. Nút chuyển sang Đăng ký
        gbc.gridy++;
        gbc.insets = new Insets(2, 6, 14, 6);
        btnGoRegister = new BrutalUI.BrutalButton("Chưa có tài khoản? Đăng ký ngay →", BrutalUI.BrutalButton.Style.OUTLINE);
        btnGoRegister.setPreferredSize(new Dimension(360, 38));
        btnGoRegister.setFont(BrutalUI.FONT_SMALL_BOLD);
        btnGoRegister.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_REGISTER));
        card.add(btnGoRegister, gbc);

        // 9. Badge Bảo mật mã hóa
        gbc.gridy++;
        gbc.insets = new Insets(6, 6, 4, 6);
        JPanel secRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        secRow.setOpaque(false);
        JLabel lblSecurity = BrutalUI.createBadge("KÊNH BẢO MẬT: RSA-2048 + AES-256 GCM",
                new Color(220, 252, 231), new Color(21, 128, 61));
        secRow.add(lblSecurity);
        card.add(secRow, gbc);

        add(card);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        lblStatus.setForeground(BrutalUI.COLOR_INFO_TEXT);
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
                    lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
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
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText(message != null ? message : "Đăng nhập thất bại!");
            if (message != null && message.contains("đã được đăng nhập")) {
                JOptionPane.showMessageDialog(this, message, "Cảnh Báo Đăng Nhập", JOptionPane.WARNING_MESSAGE);
            } else if (message != null && message.contains("chưa được kích hoạt")) {
                int opt = JOptionPane.showConfirmDialog(
                        this,
                        "Tài khoản chưa được kích hoạt!\n\n"
                                + "Mã OTP xác thực đã được gửi về Cơ sở dữ liệu (Bảng OTP, thời hạn 10 phút).\n"
                                + "Vui lòng mở Cơ sở dữ liệu (bảng OTP) để lấy mã xác thực.\n\n"
                                + "Bạn có muốn chuyển sang màn hình kích hoạt để nhập mã OTP ngay không?",
                        "Kích hoạt tài khoản (Mã OTP từ CSDL)",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (opt == JOptionPane.YES_OPTION) {
                    mainFrame.showPanel(MainClientFrame.CARD_REGISTER);
                }
            }
        }
    }
}
