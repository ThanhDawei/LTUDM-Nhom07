package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.util.AvatarUtil;
import com.quizzlive.client.util.BrutalUI;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng ký tài khoản và Xác thực OTP - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế Card trắng viền đen 2px, bóng cứng 5px, tương phản cao, badge nổi bật
 * Phụ trách Yêu cầu 1: "Đăng ký, đăng nhập, cập nhật thông tin như đề 01, có xác thực email bằng OTP"
 */
public class RegisterPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private String selectedAvatar = "avatar1.png";
    private JLabel lblRegAvatarPreview;
    private JLabel lblAvatarFileName;
    private BrutalUI.BrutalButton btnBrowseAvatar;
    private JTextField txtBio;
    private BrutalUI.BrutalButton btnRegister;
    private BrutalUI.BrutalButton btnBackToLogin;
    private JLabel lblStatus;

    private String pendingEmail;

    public RegisterPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(BrutalUI.COLOR_BG);

        // Card chính Neo-Brutalist
        BrutalUI.CardPanel card = new BrutalUI.CardPanel(16, 5);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // 1. Badge Neo-Brutalist
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeRow.setOpaque(false);
        JLabel badge = BrutalUI.createBadge("ĐĂNG KÝ THÀNH VIÊN", new Color(220, 252, 231), new Color(21, 128, 61));
        badgeRow.add(badge);
        card.add(badgeRow, gbc);

        // 2. Tiêu đề
        gbc.gridy++;
        gbc.insets = new Insets(8, 6, 2, 6);
        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblTitle.setFont(BrutalUI.FONT_TITLE_XL);
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblTitle, gbc);

        // 3. Phụ đề
        gbc.gridy++;
        gbc.insets = new Insets(0, 6, 12, 6);
        JLabel lblSubtitle = new JLabel("Nhập email chính xác để nhận mã xác thực OTP (thời hạn 10 phút)", SwingConstants.CENTER);
        lblSubtitle.setFont(BrutalUI.FONT_SMALL);
        lblSubtitle.setForeground(BrutalUI.COLOR_MUTED);
        card.add(lblSubtitle, gbc);

        // 4. Form fields
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Tên đăng nhập
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(BrutalUI.FONT_BODY_BOLD);
        lblUser.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = createStyledTextField(18);
        card.add(txtUsername, gbc);

        // Mật khẩu
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(BrutalUI.FONT_BODY_BOLD);
        lblPass.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(18);
        txtPassword.setFont(BrutalUI.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(240, 36));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        txtPassword.addActionListener(e -> handleRegister());
        card.add(txtPassword, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblEmail = new JLabel("Địa chỉ Email:");
        lblEmail.setFont(BrutalUI.FONT_BODY_BOLD);
        lblEmail.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblEmail, gbc);

        gbc.gridx = 1;
        txtEmail = createStyledTextField(18);
        card.add(txtEmail, gbc);

        // Họ và tên
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblFullName = new JLabel("Họ và tên:");
        lblFullName.setFont(BrutalUI.FONT_BODY_BOLD);
        lblFullName.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblFullName, gbc);

        gbc.gridx = 1;
        txtFullName = createStyledTextField(18);
        card.add(txtFullName, gbc);

        // Ảnh đại diện
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblAvatarTitle = new JLabel("Ảnh đại diện:");
        lblAvatarTitle.setFont(BrutalUI.FONT_BODY_BOLD);
        lblAvatarTitle.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblAvatarTitle, gbc);

        gbc.gridx = 1;
        btnBrowseAvatar = new BrutalUI.BrutalButton("Chọn ảnh...", BrutalUI.BrutalButton.Style.OUTLINE);
        btnBrowseAvatar.setPreferredSize(new Dimension(110, 32));
        btnBrowseAvatar.setFont(BrutalUI.FONT_SMALL_BOLD);

        lblRegAvatarPreview = new JLabel(AvatarUtil.getAvatarIcon(selectedAvatar, 32));
        lblAvatarFileName = new JLabel("Mặc định (avatar1.png)");
        lblAvatarFileName.setFont(BrutalUI.FONT_SMALL);
        lblAvatarFileName.setForeground(BrutalUI.COLOR_MUTED);

        btnBrowseAvatar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn ảnh đại diện từ máy tính");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
            int ret = chooser.showOpenDialog(RegisterPanel.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                selectedAvatar = AvatarUtil.saveCustomAvatar(file, "reg_avatar");
                lblAvatarFileName.setText(file.getName());
                lblRegAvatarPreview.setIcon(AvatarUtil.getAvatarIcon(selectedAvatar, 32));
            }
        });

        JPanel regAvatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        regAvatarRow.setOpaque(false);
        regAvatarRow.add(btnBrowseAvatar);
        regAvatarRow.add(lblRegAvatarPreview);
        regAvatarRow.add(lblAvatarFileName);
        card.add(regAvatarRow, gbc);

        // Giới thiệu
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblBio = new JLabel("Giới thiệu:");
        lblBio.setFont(BrutalUI.FONT_BODY_BOLD);
        lblBio.setForeground(BrutalUI.COLOR_BLACK);
        card.add(lblBio, gbc);

        gbc.gridx = 1;
        txtBio = createStyledTextField(18);
        txtBio.setText("Đam mê kiến thức và trắc nghiệm");
        card.add(txtBio, gbc);

        // Status Label
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 6, 6, 6);
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(BrutalUI.FONT_SMALL_BOLD);
        lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
        card.add(lblStatus, gbc);

        // Register Button
        gbc.gridy++;
        gbc.insets = new Insets(4, 6, 6, 6);
        btnRegister = new BrutalUI.BrutalButton("ĐĂNG KÝ & NHẬN OTP", BrutalUI.BrutalButton.Style.PRIMARY);
        btnRegister.setPreferredSize(new Dimension(380, 42));
        btnRegister.setFont(BrutalUI.FONT_BODY_BOLD);
        btnRegister.addActionListener(e -> handleRegister());
        card.add(btnRegister, gbc);

        // Back to Login Button
        gbc.gridy++;
        gbc.insets = new Insets(2, 6, 6, 6);
        btnBackToLogin = new BrutalUI.BrutalButton("← Đã có tài khoản? Quay lại đăng nhập", BrutalUI.BrutalButton.Style.OUTLINE);
        btnBackToLogin.setPreferredSize(new Dimension(380, 36));
        btnBackToLogin.setFont(BrutalUI.FONT_SMALL_BOLD);
        btnBackToLogin.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOGIN));
        card.add(btnBackToLogin, gbc);

        add(card);
    }

    private JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(BrutalUI.FONT_BODY);
        tf.setPreferredSize(new Dimension(240, 36));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        tf.addActionListener(e -> handleRegister());
        return tf;
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String avatar = (selectedAvatar != null && !selectedAvatar.trim().isEmpty()) ? selectedAvatar.trim() : "avatar1.png";
        String bio = txtBio.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText("Vui lòng điền đầy đủ tất cả các trường!");
            return;
        }

        if (username.length() < 3) {
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText("Tên đăng nhập phải có tối thiểu 3 ký tự!");
            return;
        }

        if (password.length() < 6) {
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText("Mật khẩu phải có tối thiểu 6 ký tự!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText("Định dạng email không hợp lệ!");
            return;
        }

        lblStatus.setForeground(BrutalUI.COLOR_INFO_TEXT);
        lblStatus.setText("Đang gửi yêu cầu đăng ký...");
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
                    lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
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
            lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblStatus.setText(message != null ? message : "Đăng ký thất bại!");
        }
    }

    /**
     * Hiển thị popup nhập OTP kích hoạt tài khoản
     */
    private void promptOtpDialog() {
        String msg = "Mã OTP xác thực đã được gửi về Cơ sở dữ liệu (Bảng OTP)!\n\n"
                + "• Email đăng ký: " + pendingEmail + "\n"
                + "• Thời hạn hiệu lực: 10 phút kể từ lúc tạo\n\n"
                + "Vui lòng mở Cơ sở dữ liệu (bảng OTP) để lấy mã xác thực và nhập vào đây để kích hoạt tài khoản:";

        String otp = JOptionPane.showInputDialog(
                this,
                msg,
                "Xác thực mã OTP từ CSDL (Hiệu lực 10 phút)",
                JOptionPane.QUESTION_MESSAGE
        );

        if (otp != null && !otp.trim().isEmpty()) {
            lblStatus.setForeground(BrutalUI.COLOR_INFO_TEXT);
            lblStatus.setText("Đang xác thực mã OTP từ CSDL...");

            new Thread(() -> {
                try {
                    if (!SocketClient.getInstance().isConnected()) {
                        SocketClient.getInstance().connect();
                    }
                    authService.sendVerifyOtp(pendingEmail, otp.trim());
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(BrutalUI.COLOR_WRONG_TEXT);
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
