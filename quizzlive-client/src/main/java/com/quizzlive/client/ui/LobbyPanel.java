package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.service.ClientChallengeService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.shared.dto.AuthDTO;
import com.quizzlive.shared.dto.ChallengeDTO;
import com.quizzlive.shared.dto.TopicDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Sảnh chờ (Lobby)
 * Phụ trách:
 * - Yêu cầu 1: Cập nhật thông tin cá nhân, đăng xuất
 * - Yêu cầu 2: Nút tìm trận (ghép cặp tự động)
 * - Yêu cầu 3: Thách đấu bạn bè đang online
 * - Yêu cầu 4: Chọn chủ đề và độ khó câu hỏi
 * - Điều hướng sang Yêu cầu 9 (Giải đấu) và Yêu cầu 10 (Bảng xếp hạng)
 */
public class LobbyPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientAuthService authService = new ClientAuthService();
    private final ClientMatchmakingService matchmakingService = new ClientMatchmakingService();
    private final ClientChallengeService challengeService = new ClientChallengeService();

    private JLabel lblWelcome;
    private JLabel lblUserStats;
    private JComboBox<TopicItem> cbTopics;
    private JComboBox<String> cbDifficulty;
    private JTable tblOnlineUsers;
    private DefaultTableModel modelOnlineUsers;
    private JButton btnFindMatch;
    private JButton btnChallenge;

    private Long currentUserId;
    private String currentUsername;

    public static class TopicItem {
        public final int id;
        public final String name;

        public TopicItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public LobbyPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(new Color(245, 247, 250));

        // 1. TOP HEADER PANEL (User info & action buttons)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel userBox = new JPanel(new GridLayout(2, 1));
        userBox.setOpaque(false);
        lblWelcome = new JLabel("Xin chào, Người chơi!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblWelcome.setForeground(new Color(41, 128, 185));

        lblUserStats = new JLabel("Điểm IQ: -- | Thắng: 0 | Chuỗi thắng: 0");
        lblUserStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserStats.setForeground(Color.DARK_GRAY);

        userBox.add(lblWelcome);
        userBox.add(lblUserStats);
        topPanel.add(userBox, BorderLayout.WEST);

        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navActions.setOpaque(false);

        JButton btnLeaderboard = new JButton("🏆 Bảng Xếp Hạng");
        btnLeaderboard.setFocusPainted(false);
        btnLeaderboard.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LEADERBOARD));

        JButton btnTournament = new JButton("🎖️ Giải Đấu");
        btnTournament.setFocusPainted(false);
        btnTournament.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_TOURNAMENT));

        JButton btnProfile = new JButton("👤 Cập Nhật Hồ Sơ");
        btnProfile.setFocusPainted(false);
        btnProfile.addActionListener(e -> showUpdateProfileDialog());

        JButton btnLogout = new JButton("Đăng Xuất");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            authService.sendLogout();
            mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
        });

        navActions.add(btnLeaderboard);
        navActions.add(btnTournament);
        navActions.add(btnProfile);
        navActions.add(btnLogout);
        topPanel.add(navActions, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 2. CENTER CONTENT (Split into Match Options & Online Users)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Left Card: Find Match & Game Config (Yêu cầu 2, 4)
        JPanel matchCard = new JPanel(new GridBagLayout());
        matchCard.setBackground(Color.WHITE);
        matchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblMatchTitle = new JLabel("CHỌN THÔNG SỐ VÀ TÌM TRẬN ĐẤU", SwingConstants.CENTER);
        lblMatchTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMatchTitle.setForeground(new Color(52, 73, 94));
        matchCard.add(lblMatchTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        matchCard.add(new JLabel("Chủ đề câu hỏi (YC 4):"), gbc);
        gbc.gridx = 1;
        cbTopics = new JComboBox<>(new TopicItem[]{
                new TopicItem(0, "-- Tất cả chủ đề --"),
                new TopicItem(1, "Toán học & Logic"),
                new TopicItem(2, "Lịch sử & Địa lý"),
                new TopicItem(3, "Khoa học Tự nhiên"),
                new TopicItem(4, "Tin học & Công nghệ")
        });
        matchCard.add(cbTopics, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        matchCard.add(new JLabel("Độ khó (YC 4):"), gbc);
        gbc.gridx = 1;
        cbDifficulty = new JComboBox<>(new String[]{"Mọi cấp độ", "Dễ", "Trung bình", "Khó"});
        matchCard.add(cbDifficulty, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnFindMatch = new JButton("⚔️ TÌM TRẬN ĐẤU (Ghép tự động 60s)");
        btnFindMatch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFindMatch.setBackground(new Color(41, 128, 185));
        btnFindMatch.setForeground(Color.WHITE);
        btnFindMatch.setPreferredSize(new Dimension(200, 45));
        btnFindMatch.setFocusPainted(false);
        btnFindMatch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFindMatch.addActionListener(e -> startFindingMatch());
        matchCard.add(btnFindMatch, gbc);

        centerPanel.add(matchCard);

        // Right Card: Online Users List & Challenge (Yêu cầu 3)
        JPanel onlineCard = new JPanel(new BorderLayout(8, 8));
        onlineCard.setBackground(Color.WHITE);
        onlineCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel onlineHeader = new JPanel(new BorderLayout());
        onlineHeader.setOpaque(false);
        JLabel lblOnlineTitle = new JLabel("Danh sách Online (Thách đấu - YC 3)");
        lblOnlineTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOnlineTitle.setForeground(new Color(52, 73, 94));
        JButton btnRefreshOnline = new JButton("Làm mới");
        btnRefreshOnline.addActionListener(e -> authService.requestOnlineUsers());
        onlineHeader.add(lblOnlineTitle, BorderLayout.WEST);
        onlineHeader.add(btnRefreshOnline, BorderLayout.EAST);
        onlineCard.add(onlineHeader, BorderLayout.NORTH);

        modelOnlineUsers = new DefaultTableModel(new String[]{"ID", "Tên người chơi", "Điểm IQ", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOnlineUsers = new JTable(modelOnlineUsers);
        tblOnlineUsers.setRowHeight(24);
        onlineCard.add(new JScrollPane(tblOnlineUsers), BorderLayout.CENTER);

        btnChallenge = new JButton("⚡ Mời Thách Đấu Người Này");
        btnChallenge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChallenge.setBackground(new Color(230, 126, 34));
        btnChallenge.setForeground(Color.WHITE);
        btnChallenge.setFocusPainted(false);
        btnChallenge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChallenge.addActionListener(e -> handleSendChallenge());
        onlineCard.add(btnChallenge, BorderLayout.SOUTH);

        centerPanel.add(onlineCard);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Bắt đầu ghép cặp tự động (Yêu cầu 2, 4)
     */
    private void startFindingMatch() {
        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = (String) cbDifficulty.getSelectedItem();

        // Gửi gói tin QUEUE_JOIN_REQ lên server
        matchmakingService.joinQueue(topicId, difficulty);

        // Chuyển sang màn hình chờ ghép cặp 60s
        mainFrame.getWaitingPanel().startCountdown(60);
        mainFrame.showPanel(MainClientFrame.CARD_WAITING);
    }

    /**
     * Gửi lời mời thách đấu bạn bè (Yêu cầu 3)
     */
    private void handleSendChallenge() {
        int selectedRow = tblOnlineUsers.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi online từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long targetUserId = Long.parseLong(modelOnlineUsers.getValueAt(selectedRow, 0).toString());
        String targetUsername = modelOnlineUsers.getValueAt(selectedRow, 1).toString();

        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = (String) cbDifficulty.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn gửi lời mời thách đấu tới " + targetUsername + "?",
                "Xác nhận thách đấu",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            challengeService.sendChallenge(targetUserId, topicId, difficulty);
            JOptionPane.showMessageDialog(this, "Đã gửi lời mời thách đấu tới " + targetUsername + ". Vui lòng chờ phản hồi!");
        }
    }

    /**
     * Popup khi nhận được lời mời thách đấu từ người khác (Yêu cầu 3)
     */
    public void showIncomingChallengeDialog(ChallengeDTO.InviteNotify invite) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Người chơi [" + invite.fromUsername + "] đang thách đấu bạn!\nĐộ khó: " + invite.difficulty + "\n\nBạn có chấp nhận thi đấu không?",
                "Lời Mời Thách Đấu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            challengeService.acceptChallenge(invite.invitationId);
        } else {
            challengeService.rejectChallenge(invite.invitationId);
        }
    }

    /**
     * Cập nhật thông tin cá nhân (Yêu cầu 1)
     */
    private void showUpdateProfileDialog() {
        JTextField txtName = new JTextField(20);
        JPasswordField txtCurPass = new JPasswordField(20);
        JPasswordField txtNewPass = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Họ tên mới:"));
        panel.add(txtName);
        panel.add(new JLabel("Mật khẩu hiện tại:"));
        panel.add(txtCurPass);
        panel.add(new JLabel("Mật khẩu mới (nếu đổi):"));
        panel.add(txtNewPass);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cập nhật thông tin cá nhân (YC 1)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            authService.sendUpdateProfile(txtName.getText().trim(), new String(txtCurPass.getPassword()), new String(txtNewPass.getPassword()), null);
        }
    }

    public void updateUserInfo(AuthDTO.AuthResponse auth) {
        this.currentUserId = auth.userId;
        this.currentUsername = auth.username;
        lblWelcome.setText("Xin chào, " + (auth.fullName != null ? auth.fullName : auth.username) + "!");
        lblUserStats.setText("Điểm IQ: " + (auth.iqScore != null ? auth.iqScore : "Chưa test"));
        authService.requestOnlineUsers();
    }

    public void updateOnlineUsersList(List<AuthDTO.AuthResponse> users) {
        modelOnlineUsers.setRowCount(0);
        if (users != null) {
            for (AuthDTO.AuthResponse u : users) {
                modelOnlineUsers.addRow(new Object[]{u.userId, u.username, u.iqScore != null ? u.iqScore : "-", "Online"});
            }
        }
    }
}
