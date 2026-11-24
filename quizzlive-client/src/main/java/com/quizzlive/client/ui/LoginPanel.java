package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.util.BrutalUI;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng nhập phía Client - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế tinh gọn, tập trung vào Form Đăng nhập
 * Phụ trách: Thành viên 1 · Yêu cầu 1 (Xác thực tài khoản & Đăng nhập)
 */
public class LoginPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private BrutalUI.BrutalButton btnLogin;
    private JButton btnGoRegister;
    private JLabel lblStatus;

    public LoginPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(BrutalUI.COLOR_BG);

        // =========================================================================
        // CARD ĐĂNG NHẬP TRUNG TÂM (NEO-BRUTALIST)
        // =========================================================================
        BrutalUI.CardPanel loginCard = new BrutalUI.CardPanel(16, 4);
        loginCard.setLayout(new BorderLayout(15, 14));
        loginCard.setPreferredSize(new Dimension(450, 480));
        loginCard.setMaximumSize(new Dimension(460, 500));
        loginCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Header form: Logo Box + Tiêu đề
        JPanel formHeader = new JPanel();
        formHeader.setOpaque(false);
        formHeader.setLayout(new BoxLayout(formHeader, BoxLayout.Y_AXIS));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        logoRow.add(createLogoBadge());

        JLabel lblFormTitle = new JLabel("ĐĂNG NHẬP TÀI KHOẢN");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblFormTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblFormTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblFormSub = new JLabel("Tranh tài trắc nghiệm thời gian thực QuizzLive");
        lblFormSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormSub.setForeground(BrutalUI.COLOR_MUTED);
        lblFormSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        formHeader.add(logoRow);
        formHeader.add(Box.createVerticalStrut(10));
        formHeader.add(lblFormTitle);
        formHeader.add(Box.createVerticalStrut(4));
        formHeader.add(lblFormSub);

        loginCard.add(formHeader, BorderLayout.NORTH);

        // Body form: Các ô nhập liệu
        JPanel formBody = new JPanel();
        formBody.setOpaque(false);
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));

        JLabel lblUserTag = new JLabel("TÊN ĐĂNG NHẬP / EMAIL");
        lblUserTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUserTag.setForeground(BrutalUI.COLOR_BLACK);
        lblUserTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = createStyledTextField();
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblPassTag = new JLabel("MẬT KHẨU");
        lblPassTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPassTag.setForeground(BrutalUI.COLOR_BLACK);
        lblPassTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = createStyledPasswordField();
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(220, 38, 38));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBody.add(Box.createVerticalStrut(12));
        formBody.add(lblUserTag);
        formBody.add(Box.createVerticalStrut(6));
        formBody.add(txtUsername);
        formBody.add(Box.createVerticalStrut(14));
        formBody.add(lblPassTag);
        formBody.add(Box.createVerticalStrut(6));
        formBody.add(txtPassword);
        formBody.add(Box.createVerticalStrut(10));
        formBody.add(lblStatus);

        loginCard.add(formBody, BorderLayout.CENTER);

        // Footer form: Nút bấm Đăng nhập & Đăng ký
        JPanel formFooter = new JPanel();
        formFooter.setOpaque(false);
        formFooter.setLayout(new BoxLayout(formFooter, BoxLayout.Y_AXIS));

        btnLogin = new BrutalUI.BrutalButton("ĐĂNG NHẬP NGAY >>", BrutalUI.BrutalButton.Style.PRIMARY);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        btnGoRegister = new JButton("Chưa có tài khoản? Đăng ký mới nhận OTP");
        btnGoRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGoRegister.setBorderPainted(false);
        btnGoRegister.setContentAreaFilled(false);
        btnGoRegister.setFocusPainted(false);
        btnGoRegister.setForeground(new Color(37, 99, 235));
        btnGoRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoRegister.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_REGISTER));

        formFooter.add(btnLogin);
        formFooter.add(Box.createVerticalStrut(8));
        formFooter.add(btnGoRegister);

        loginCard.add(formFooter, BorderLayout.SOUTH);

        add(loginCard);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tf.setForeground(BrutalUI.COLOR_BLACK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(BrutalUI.COLOR_BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        tf.addActionListener(e -> handleLogin());
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pf.setForeground(BrutalUI.COLOR_BLACK);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(BrutalUI.COLOR_BLACK);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pf.addActionListener(e -> handleLogin());
        return pf;
    }

    private JPanel createLogoBadge() {
        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 46;
                // Bóng đen 3px
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(3, 3, size, size, 12, 12);

                // Khung đen
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(0, 0, size, size, 12, 12);

                // Chữ Q trắng
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth("Q")) / 2;
                int y = (size + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString("Q", x, y);

                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(50, 50));
        logo.setMaximumSize(new Dimension(50, 50));
        return logo;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setForeground(new Color(220, 38, 38));
            lblStatus.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        lblStatus.setForeground(new Color(37, 99, 235));
        lblStatus.setText("Đang kết nối và đăng nhập...");
        btnLogin.setEnabled(false);

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendLogin(username, password);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setForeground(new Color(220, 38, 38));
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
            lblStatus.setForeground(new Color(220, 38, 38));
            lblStatus.setText(message != null ? message : "Đăng nhập thất bại!");
            if (message != null && message.contains("đã được đăng nhập")) {
                JOptionPane.showMessageDialog(this, message, "Cảnh Báo Đăng Nhập", JOptionPane.WARNING_MESSAGE);
            } else if (message != null && message.contains("chưa được kích hoạt")) {
                int opt = JOptionPane.showConfirmDialog(
                        this,
                        "Tài khoản chưa được kích hoạt!\n\n"
                                + "Mã OTP xác thực đã được gửi về email của bạn (thời hạn 10 phút).\n\n"
                                + "Bạn có muốn chuyển sang màn hình kích hoạt để nhập mã OTP ngay không?",
                        "Kích hoạt tài khoản (Xác thực OTP)",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (opt == JOptionPane.YES_OPTION) {
                    mainFrame.getRegisterPanel().showOtpPanel(txtUsername.getText().trim());
                    mainFrame.showPanel(MainClientFrame.CARD_REGISTER);
                }
            }
        }
    }
}
