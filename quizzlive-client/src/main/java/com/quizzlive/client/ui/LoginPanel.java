package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.util.BrutalUI;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng nhập phía Client - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế theo SCREEN 1 trong master_gui_showcase.html
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

        // Container 2 cột đặt ở giữa màn hình
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        centerGrid.setOpaque(false);
        centerGrid.setPreferredSize(new Dimension(860, 480));
        centerGrid.setMaximumSize(new Dimension(880, 500));

        // =========================================================================
        // 1. CỘT TRÁI: THẺ QUẢNG BÁ / THÔNG TIN HỆ THỐNG (PROMO CARD)
        // =========================================================================
        BrutalUI.CardPanel leftCard = new BrutalUI.CardPanel(16, 4);
        leftCard.setLayout(new BorderLayout(15, 15));
        leftCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));

        JPanel leftTop = new JPanel();
        leftTop.setOpaque(false);
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));

        // Logo Box "Q"
        JPanel logoBox = createLogoBadge();
        logoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppTitle = new JLabel("Đấu Trường Trí Tuệ");
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAppTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblAppTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTagline = new JLabel("Thi Đấu Trắc Nghiệm Thời Gian Thực");
        lblTagline.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTagline.setForeground(new Color(37, 99, 235));
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAppDesc = new JLabel("<html><div style='line-height:1.5; color:#52525B; font-size:12px; margin-top:4px;'>"
                + "Thi đấu đối kháng 1v1 ngẫu nhiên, giải đố nhanh 10 giây/câu, "
                + "hàng đợi ghép cặp 60 giây, giải đấu nhiều bảng và bài test IQ logic hàng ngày."
                + "</div></html>");
        lblAppDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTop.add(logoBox);
        leftTop.add(Box.createVerticalStrut(14));
        leftTop.add(lblAppTitle);
        leftTop.add(Box.createVerticalStrut(2));
        leftTop.add(lblTagline);
        leftTop.add(Box.createVerticalStrut(8));
        leftTop.add(lblAppDesc);

        // Features list
        JPanel leftCenter = new JPanel();
        leftCenter.setOpaque(false);
        leftCenter.setLayout(new BoxLayout(leftCenter, BoxLayout.Y_AXIS));

        JPanel feat1 = createFeatureItem(
                "[+] Đồng bộ Socket thuần cực nhanh",
                "Giao thức mạng TCP Socket thuần, độ trễ phản hồi < 0.05 giây."
        );
        feat1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel feat2 = createFeatureItem(
                "[*] Đấu trường 1v1 & Giải đấu theo bảng",
                "Ghép cặp tự động 60s, xáo trộn câu hỏi và test IQ hình ảnh."
        );
        feat2.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCenter.add(Box.createVerticalStrut(10));
        leftCenter.add(feat1);
        leftCenter.add(Box.createVerticalStrut(10));
        leftCenter.add(feat2);

        // Footer left card
        JLabel lblFooter = new JLabel("Lập Trình Ứng Dụng Mạng · Nhóm 07 - Đề tài 09 (SGU)");
        lblFooter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFooter.setForeground(BrutalUI.COLOR_MUTED);

        leftCard.add(leftTop, BorderLayout.NORTH);
        leftCard.add(leftCenter, BorderLayout.CENTER);
        leftCard.add(lblFooter, BorderLayout.SOUTH);

        centerGrid.add(leftCard);

        // =========================================================================
        // 2. CỘT PHẢI: FORM ĐĂNG NHẬP (FORM CARD)
        // =========================================================================
        BrutalUI.CardPanel rightCard = new BrutalUI.CardPanel(16, 4);
        rightCard.setLayout(new BorderLayout(15, 15));
        rightCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));

        // Header form
        JPanel rightHeader = new JPanel();
        rightHeader.setOpaque(false);
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));

        JLabel lblFormTitle = new JLabel("ĐĂNG NHẬP TÀI KHOẢN");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblFormTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFormSub = new JLabel("Nhập tên đăng nhập và mật khẩu để vào sảnh chờ");
        lblFormSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormSub.setForeground(BrutalUI.COLOR_MUTED);
        lblFormSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightHeader.add(lblFormTitle);
        rightHeader.add(Box.createVerticalStrut(4));
        rightHeader.add(lblFormSub);

        // Body form
        JPanel rightBody = new JPanel();
        rightBody.setOpaque(false);
        rightBody.setLayout(new BoxLayout(rightBody, BoxLayout.Y_AXIS));

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

        rightBody.add(Box.createVerticalStrut(14));
        rightBody.add(lblUserTag);
        rightBody.add(Box.createVerticalStrut(6));
        rightBody.add(txtUsername);
        rightBody.add(Box.createVerticalStrut(14));
        rightBody.add(lblPassTag);
        rightBody.add(Box.createVerticalStrut(6));
        rightBody.add(txtPassword);
        rightBody.add(Box.createVerticalStrut(10));
        rightBody.add(lblStatus);

        // Footer form (Nút đăng nhập & Đăng ký)
        JPanel rightFooter = new JPanel();
        rightFooter.setOpaque(false);
        rightFooter.setLayout(new BoxLayout(rightFooter, BoxLayout.Y_AXIS));

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

        rightFooter.add(btnLogin);
        rightFooter.add(Box.createVerticalStrut(12));
        rightFooter.add(btnGoRegister);

        rightCard.add(rightHeader, BorderLayout.NORTH);
        rightCard.add(rightBody, BorderLayout.CENTER);
        rightCard.add(rightFooter, BorderLayout.SOUTH);

        centerGrid.add(rightCard);

        add(centerGrid);
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

                int size = 42;
                // Bóng đen 3px
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(3, 3, size, size, 12, 12);

                // Khung đen
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(0, 0, size, size, 12, 12);

                // Chữ Q trắng
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth("Q")) / 2;
                int y = (size + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString("Q", x, y);

                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(48, 48));
        logo.setMaximumSize(new Dimension(48, 48));
        return logo;
    }

    private JPanel createFeatureItem(String title, String desc) {
        JPanel item = new JPanel(new BorderLayout(6, 2));
        item.setBackground(new Color(250, 250, 252));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(BrutalUI.COLOR_BLACK);

        JLabel lblD = new JLabel(desc);
        lblD.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblD.setForeground(BrutalUI.COLOR_MUTED);

        item.add(lblT, BorderLayout.NORTH);
        item.add(lblD, BorderLayout.CENTER);
        return item;
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
