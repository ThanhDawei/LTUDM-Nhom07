package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.util.AvatarUtil;
import com.quizzlive.client.util.BrutalUI;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng ký tài khoản và Xác thực OTP 6 Số - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế theo SCREEN 2 trong master_gui_showcase.html
 * Phụ trách: Thành viên 1 · Yêu cầu 1 (Đăng ký tài khoản & Xác thực OTP 10 phút)
 */
public class RegisterPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    // Form Đăng ký
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private String selectedAvatar = "avatar1.png";
    private JLabel lblRegAvatarPreview;
    private JLabel lblAvatarFileName;
    private JButton btnBrowseAvatar;
    private JTextField txtBio;
    private BrutalUI.BrutalButton btnRegister;
    private JButton btnBackToLogin;
    private JLabel lblRegStatus;

    // Card Xác thực OTP (Cột phải)
    private JLabel lblOtpEmailTarget;
    private JTextField txtOtpCode;
    private BrutalUI.BrutalButton btnVerifyOtp;
    private BrutalUI.BrutalButton btnResendOtp;
    private JLabel lblOtpStatus;

    private String pendingEmail;

    public RegisterPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(BrutalUI.COLOR_BG);

        // Container 2 cột đặt ở giữa màn hình
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        centerGrid.setOpaque(false);
        centerGrid.setPreferredSize(new Dimension(890, 530));
        centerGrid.setMaximumSize(new Dimension(920, 560));

        // =========================================================================
        // 1. CỘT TRÁI: FORM ĐĂNG KÝ (BƯỚC 1/2)
        // =========================================================================
        BrutalUI.CardPanel leftCard = new BrutalUI.CardPanel(16, 4);
        leftCard.setLayout(new BorderLayout(12, 10));
        leftCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Trái
        JPanel leftHeader = new JPanel(new BorderLayout(8, 0));
        leftHeader.setOpaque(false);
        JLabel lblLeftTitle = new JLabel("THÔNG TIN ĐĂNG KÝ");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLeftTitle.setForeground(BrutalUI.COLOR_BLACK);

        JLabel lblStep1Badge = BrutalUI.createBadge("Bước 1/2", new Color(244, 244, 245), BrutalUI.COLOR_BLACK);
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);
        leftHeader.add(lblStep1Badge, BorderLayout.EAST);
        leftCard.add(leftHeader, BorderLayout.NORTH);

        // Body Trái (Các trường nhập)
        JPanel leftBody = new JPanel();
        leftBody.setOpaque(false);
        leftBody.setLayout(new BoxLayout(leftBody, BoxLayout.Y_AXIS));

        txtUsername = createStyledField("Tên tài khoản (tối thiểu 3 ký tự)...");
        txtFullName = createStyledField("Họ và tên của bạn...");
        txtEmail = createStyledField("Email chính xác để nhận OTP...");
        txtPassword = createStyledPasswordField();
        txtBio = createStyledField("Đam mê kiến thức và trắc nghiệm");

        leftBody.add(createFieldRow("TÊN ĐĂNG NHẬP", txtUsername));
        leftBody.add(Box.createVerticalStrut(8));
        leftBody.add(createFieldRow("HỌ VÀ TÊN", txtFullName));
        leftBody.add(Box.createVerticalStrut(8));
        leftBody.add(createFieldRow("EMAIL NHẬN MÃ OTP", txtEmail));
        leftBody.add(Box.createVerticalStrut(8));
        leftBody.add(createFieldRow("MẬT KHẨU", txtPassword));
        leftBody.add(Box.createVerticalStrut(8));

        // Chọn ảnh đại diện (Neo-Brutalist Avatar Picker)
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        avatarRow.setOpaque(false);
        btnBrowseAvatar = new JButton("Chọn ảnh...");
        btnBrowseAvatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnBrowseAvatar.setBackground(Color.WHITE);
        btnBrowseAvatar.setForeground(BrutalUI.COLOR_BLACK);
        btnBrowseAvatar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btnBrowseAvatar.setFocusPainted(false);
        btnBrowseAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblRegAvatarPreview = new JLabel(AvatarUtil.getAvatarIcon(selectedAvatar, 30));
        lblAvatarFileName = new JLabel("avatar1.png");
        lblAvatarFileName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAvatarFileName.setForeground(BrutalUI.COLOR_MUTED);

        btnBrowseAvatar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn ảnh đại diện từ máy tính");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
            int ret = chooser.showOpenDialog(RegisterPanel.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                String sanitized = AvatarUtil.saveCustomAvatar(file, "reg_avatar");
                if (sanitized != null) {
                    selectedAvatar = sanitized;
                    lblAvatarFileName.setText(file.getName());
                    lblRegAvatarPreview.setIcon(AvatarUtil.getAvatarIcon(selectedAvatar, 30));
                } else {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "File được chọn không phải hình ảnh hợp lệ hoặc bị hỏng!", "Lỗi ảnh", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        avatarRow.add(btnBrowseAvatar);
        avatarRow.add(lblRegAvatarPreview);
        avatarRow.add(lblAvatarFileName);

        JPanel avatarSection = new JPanel(new BorderLayout(0, 4));
        avatarSection.setOpaque(false);
        JLabel lblAvtTitle = new JLabel("ẢNH ĐẠI DIỆN");
        lblAvtTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblAvtTitle.setForeground(BrutalUI.COLOR_BLACK);
        avatarSection.add(lblAvtTitle, BorderLayout.NORTH);
        avatarSection.add(avatarRow, BorderLayout.CENTER);
        leftBody.add(avatarSection);

        lblRegStatus = new JLabel(" ", SwingConstants.CENTER);
        lblRegStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRegStatus.setForeground(new Color(220, 38, 38));
        leftBody.add(Box.createVerticalStrut(6));
        leftBody.add(lblRegStatus);

        leftCard.add(leftBody, BorderLayout.CENTER);

        // Footer Trái (Nút đăng ký)
        JPanel leftFooter = new JPanel();
        leftFooter.setOpaque(false);
        leftFooter.setLayout(new BoxLayout(leftFooter, BoxLayout.Y_AXIS));

        btnRegister = new BrutalUI.BrutalButton("GỬI MÃ OTP ĐẾN EMAIL >>", BrutalUI.BrutalButton.Style.PRIMARY);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> handleRegister());

        btnBackToLogin = new JButton("Đã có tài khoản? Quay lại đăng nhập");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBackToLogin.setBorderPainted(false);
        btnBackToLogin.setContentAreaFilled(false);
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setForeground(new Color(37, 99, 235));
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackToLogin.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOGIN));

        leftFooter.add(btnRegister);
        leftFooter.add(Box.createVerticalStrut(8));
        leftFooter.add(btnBackToLogin);
        leftCard.add(leftFooter, BorderLayout.SOUTH);

        centerGrid.add(leftCard);

        // =========================================================================
        // 2. CỘT PHẢI: FORM XÁC THỰC MÃ OTP 6 SỐ (BƯỚC 2/2)
        // =========================================================================
        BrutalUI.CardPanel rightCard = new BrutalUI.CardPanel(16, 4);
        rightCard.setLayout(new BorderLayout(15, 14));
        rightCard.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        // Header Phải
        JPanel rightHeader = new JPanel();
        rightHeader.setOpaque(false);
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));

        JLabel badgeOtp = BrutalUI.createBadge("Mã OTP có hiệu lực trong 10 phút", new Color(254, 243, 199), new Color(146, 64, 14));
        badgeOtp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblOtpTitle = new JLabel("XÁC THỰC MÃ OTP");
        lblOtpTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblOtpTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblOtpTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblOtpEmailTarget = new JLabel("<html><div style='color:#52525B; font-size:12px; line-height:1.4;'>"
                + "Nhập mã gồm 6 chữ số đã được gửi về CSDL / Email để kích hoạt tài khoản của bạn:"
                + "</div></html>");
        lblOtpEmailTarget.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightHeader.add(badgeOtp);
        rightHeader.add(Box.createVerticalStrut(8));
        rightHeader.add(lblOtpTitle);
        rightHeader.add(Box.createVerticalStrut(4));
        rightHeader.add(lblOtpEmailTarget);
        rightCard.add(rightHeader, BorderLayout.NORTH);

        // Body Phải (Ô nhập OTP 6 số to rõ)
        JPanel rightBody = new JPanel();
        rightBody.setOpaque(false);
        rightBody.setLayout(new BoxLayout(rightBody, BoxLayout.Y_AXIS));

        JLabel lblOtpBoxTitle = new JLabel("MÃ XÁC THỰC (6 CHỮ SỐ)");
        lblOtpBoxTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblOtpBoxTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblOtpBoxTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtOtpCode = new JTextField(6);
        txtOtpCode.setHorizontalAlignment(JTextField.CENTER);
        txtOtpCode.setFont(new Font("Segoe UI", Font.BOLD, 26));
        txtOtpCode.setForeground(BrutalUI.COLOR_BLACK);
        txtOtpCode.setBackground(Color.WHITE);
        txtOtpCode.setCaretColor(BrutalUI.COLOR_BLACK);
        txtOtpCode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtOtpCode.setMaximumSize(new Dimension(280, 52));
        txtOtpCode.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtOtpCode.addActionListener(e -> handleVerifyOtp());

        lblOtpStatus = new JLabel("Chưa gửi yêu cầu xác thực", SwingConstants.CENTER);
        lblOtpStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblOtpStatus.setForeground(BrutalUI.COLOR_MUTED);
        lblOtpStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightBody.add(Box.createVerticalStrut(18));
        rightBody.add(lblOtpBoxTitle);
        rightBody.add(Box.createVerticalStrut(8));
        rightBody.add(txtOtpCode);
        rightBody.add(Box.createVerticalStrut(12));
        rightBody.add(lblOtpStatus);

        rightCard.add(rightBody, BorderLayout.CENTER);

        // Footer Phải (Nút xác nhận & Nút gửi lại)
        JPanel rightFooter = new JPanel(new GridLayout(1, 2, 10, 0));
        rightFooter.setOpaque(false);

        btnResendOtp = new BrutalUI.BrutalButton("Gửi Lại Mã OTP", BrutalUI.BrutalButton.Style.OUTLINE);
        btnResendOtp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnResendOtp.setPreferredSize(new Dimension(0, 42));
        btnResendOtp.addActionListener(e -> handleResendOtp());

        btnVerifyOtp = new BrutalUI.BrutalButton("Xác Nhận Kích Hoạt >>", BrutalUI.BrutalButton.Style.SUCCESS);
        btnVerifyOtp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerifyOtp.setPreferredSize(new Dimension(0, 42));
        btnVerifyOtp.addActionListener(e -> handleVerifyOtp());

        rightFooter.add(btnResendOtp);
        rightFooter.add(btnVerifyOtp);
        rightCard.add(rightFooter, BorderLayout.SOUTH);

        centerGrid.add(rightCard);

        add(centerGrid);
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(BrutalUI.COLOR_BLACK);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tf.setForeground(BrutalUI.COLOR_BLACK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(BrutalUI.COLOR_BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setToolTipText(placeholder);
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pf.setForeground(BrutalUI.COLOR_BLACK);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(BrutalUI.COLOR_BLACK);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String avatar = (selectedAvatar != null && !selectedAvatar.trim().isEmpty()) ? selectedAvatar.trim() : "avatar1.png";
        String bio = txtBio.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            lblRegStatus.setForeground(new Color(220, 38, 38));
            lblRegStatus.setText("Vui lòng điền đầy đủ tất cả các trường!");
            return;
        }

        if (username.length() < 3) {
            lblRegStatus.setForeground(new Color(220, 38, 38));
            lblRegStatus.setText("Tên đăng nhập phải có tối thiểu 3 ký tự!");
            return;
        }

        if (password.length() < 6) {
            lblRegStatus.setForeground(new Color(220, 38, 38));
            lblRegStatus.setText("Mật khẩu phải có tối thiểu 6 ký tự!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            lblRegStatus.setForeground(new Color(220, 38, 38));
            lblRegStatus.setText("Định dạng email không hợp lệ!");
            return;
        }

        lblRegStatus.setForeground(new Color(37, 99, 235));
        lblRegStatus.setText("Đang gửi yêu cầu đăng ký...");
        btnRegister.setEnabled(false);
        this.pendingEmail = email;

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendRegister(username, password, email, fullName, avatar, bio);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblRegStatus.setForeground(new Color(220, 38, 38));
                    lblRegStatus.setText("Không thể kết nối đến máy chủ: " + e.getMessage());
                    btnRegister.setEnabled(true);
                });
            }
        }, "Client-Register-Worker").start();
    }

    private void handleVerifyOtp() {
        if (pendingEmail == null || pendingEmail.trim().isEmpty()) {
            lblOtpStatus.setForeground(new Color(220, 38, 38));
            lblOtpStatus.setText("Vui lòng đăng ký trước khi nhập mã OTP!");
            return;
        }

        String otp = txtOtpCode.getText().trim();
        if (otp.isEmpty() || otp.length() < 4) {
            lblOtpStatus.setForeground(new Color(220, 38, 38));
            lblOtpStatus.setText("Vui lòng nhập đầy đủ mã OTP 6 số!");
            return;
        }

        lblOtpStatus.setForeground(new Color(37, 99, 235));
        lblOtpStatus.setText("Đang xác thực mã OTP từ máy chủ...");
        btnVerifyOtp.setEnabled(false);

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendVerifyOtp(pendingEmail, otp);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblOtpStatus.setForeground(new Color(220, 38, 38));
                    lblOtpStatus.setText("Lỗi mạng khi gửi OTP: " + e.getMessage());
                    btnVerifyOtp.setEnabled(true);
                });
            }
        }, "Client-VerifyOtp-Worker").start();
    }

    private void handleResendOtp() {
        if (pendingEmail == null || pendingEmail.trim().isEmpty()) {
            lblOtpStatus.setForeground(new Color(220, 38, 38));
            lblOtpStatus.setText("Chưa có email để gửi lại mã OTP!");
            return;
        }

        lblOtpStatus.setForeground(new Color(37, 99, 235));
        lblOtpStatus.setText("Đang yêu cầu gửi lại mã OTP...");
        btnResendOtp.setEnabled(false);

        new Thread(() -> {
            try {
                if (!SocketClient.getInstance().isConnected()) {
                    SocketClient.getInstance().connect();
                }
                authService.sendResendOtp(pendingEmail);
                SwingUtilities.invokeLater(() -> {
                    lblOtpStatus.setForeground(new Color(22, 163, 74));
                    lblOtpStatus.setText("Đã yêu cầu gửi lại OTP cho: " + pendingEmail);
                    btnResendOtp.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblOtpStatus.setForeground(new Color(220, 38, 38));
                    lblOtpStatus.setText("Lỗi khi gửi lại OTP: " + e.getMessage());
                    btnResendOtp.setEnabled(true);
                });
            }
        }, "Client-ResendOtp-Worker").start();
    }

    /**
     * Callback được gọi bởi ClientPacketHandler khi server phản hồi REGISTER_RESP
     */
    public void onRegisterResponse(boolean success, String message) {
        btnRegister.setEnabled(true);
        if (success) {
            lblRegStatus.setForeground(new Color(22, 163, 74));
            lblRegStatus.setText("Đăng ký thành công! Hãy nhập OTP bên phải.");

            lblOtpEmailTarget.setText("<html><div style='color:#16a34a; font-size:12px; line-height:1.4;'>"
                    + "Đã gửi mã OTP đến: <b>" + pendingEmail + "</b> (kiểm tra CSDL bảng OTP)."
                    + "</div></html>");

            lblOtpStatus.setForeground(new Color(37, 99, 235));
            lblOtpStatus.setText("Mã OTP đã tạo, hiệu lực trong 10 phút.");
            txtOtpCode.setText("");
            txtOtpCode.requestFocusInWindow();

            JOptionPane.showMessageDialog(
                    this,
                    "Mã OTP đã được gửi về CSDL (Bảng OTP) cho email: " + pendingEmail + "!\n"
                            + "Thời hạn hiệu lực: 10 phút.\n"
                            + "Vui lòng nhập 6 số vào ô 'XÁC THỰC MÃ OTP' ở bên phải để kích hoạt.",
                    "Xác Thực OTP",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            lblRegStatus.setForeground(new Color(220, 38, 38));
            lblRegStatus.setText(message != null ? message : "Đăng ký thất bại!");
        }
    }

    /**
     * Callback được gọi bởi ClientPacketHandler khi server phản hồi VERIFY_OTP_RESP
     */
    public void onVerifyOtpResponse(boolean success, String message) {
        btnVerifyOtp.setEnabled(true);
        if (success) {
            lblOtpStatus.setForeground(new Color(22, 163, 74));
            lblOtpStatus.setText("Kích hoạt thành công! Đang chuyển đăng nhập...");
            JOptionPane.showMessageDialog(this, "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
            txtPassword.setText("");
            txtOtpCode.setText("");
            mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
        } else {
            lblOtpStatus.setForeground(new Color(220, 38, 38));
            lblOtpStatus.setText("Xác thực thất bại: " + (message != null ? message : "Mã OTP không hợp lệ"));
            JOptionPane.showMessageDialog(this, "Xác thực OTP thất bại: " + message, "Lỗi OTP", JOptionPane.ERROR_MESSAGE);
            txtOtpCode.requestFocusInWindow();
        }
    }
}
