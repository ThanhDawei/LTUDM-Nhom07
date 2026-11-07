package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.service.ClientChallengeService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.shared.dto.AuthDTO;
import com.quizzlive.shared.dto.ChallengeDTO;
import com.quizzlive.shared.dto.TopicDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Sảnh Chờ (Lobby Dashboard) - Chuẩn phong cách NEO-BRUTALIST (Nền xám kem, Card trắng viền đen)
 * Theo thiết kế SCREEN 3 trong master_gui_showcase.html
 * 
 * Phụ trách:
 * - Yêu cầu 1: Cập nhật thông tin cá nhân, đăng xuất
 * - Yêu cầu 2: Nút tìm trận (ghép cặp tự động 60s)
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
    private BrutalUI.BrutalButton btnFindMatch;
    private BrutalUI.BrutalButton btnChallenge;

    // Quick Stats Components
    private JLabel lblStatTotal;
    private JLabel lblStatWinRate;
    private JLabel lblStatIq;

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
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        setBackground(BrutalUI.COLOR_BG);

        // =========================================================================
        // 1. TOP HEADER: Navigation Bar, Logo Q, User Info & Action Buttons
        // =========================================================================
        BrutalUI.CardPanel topPanel = new BrutalUI.CardPanel(16, 3);
        topPanel.setLayout(new BorderLayout(15, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // Brand & User Profile Info
        JPanel brandAndUserBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandAndUserBox.setOpaque(false);

        // Logo Icon "Q" Box
        JPanel logoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Q";
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoBox.setPreferredSize(new Dimension(38, 38));
        brandAndUserBox.add(logoBox);

        JPanel userTextBox = new JPanel(new GridLayout(2, 1, 0, 2));
        userTextBox.setOpaque(false);
        lblWelcome = new JLabel("Xin chào, Người chơi!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWelcome.setForeground(BrutalUI.COLOR_BLACK);

        lblUserStats = new JLabel("🏆 Điểm IQ: -- | Thắng: 0 | Chuỗi thắng: 0");
        lblUserStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserStats.setForeground(BrutalUI.COLOR_MUTED);

        userTextBox.add(lblWelcome);
        userTextBox.add(lblUserStats);
        brandAndUserBox.add(userTextBox);

        topPanel.add(brandAndUserBox, BorderLayout.WEST);

        // Action Navigation Buttons
        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navActions.setOpaque(false);

        BrutalUI.BrutalButton btnLeaderboard = new BrutalUI.BrutalButton("🏆 Bảng Xếp Hạng", BrutalUI.BrutalButton.Style.OUTLINE);
        btnLeaderboard.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLeaderboard.addActionListener(e -> {
            mainFrame.getLeaderboardPanel().refreshData(currentUserId);
            mainFrame.showPanel(MainClientFrame.CARD_LEADERBOARD);
        });

        BrutalUI.BrutalButton btnTournament = new BrutalUI.BrutalButton("🎖️ Giải Đấu", BrutalUI.BrutalButton.Style.OUTLINE);
        btnTournament.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTournament.addActionListener(e -> {
            new com.quizzlive.client.service.ClientTournamentService().requestTournamentList();
            mainFrame.showPanel(MainClientFrame.CARD_TOURNAMENT);
        });

        BrutalUI.BrutalButton btnProfile = new BrutalUI.BrutalButton("👤 Hồ Sơ", BrutalUI.BrutalButton.Style.OUTLINE);
        btnProfile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProfile.addActionListener(e -> showUpdateProfileDialog());

        BrutalUI.BrutalButton btnLogout = new BrutalUI.BrutalButton("Đăng Xuất", BrutalUI.BrutalButton.Style.DANGER);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        // =========================================================================
        // 2. CENTER CONTENT: Cột Trái (Task 2 & 4 Tìm Trận) + Cột Phải (Task 3 Online)
        // =========================================================================
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // --- CỘT TRÁI: Tìm Trận Đấu Nhanh (Ghép Tự Động 60s & Chọn Chủ Đề) ---
        BrutalUI.CardPanel matchCard = new BrutalUI.CardPanel(16, 4);
        matchCard.setLayout(new BorderLayout(14, 14));
        matchCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Header Match Card
        JPanel matchHeader = new JPanel(new BorderLayout());
        matchHeader.setOpaque(false);

        JLabel lblMatchTitle = new JLabel("Tìm Trận Đấu Nhanh (Ghép Tự Động)");
        lblMatchTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMatchTitle.setForeground(BrutalUI.COLOR_BLACK);

        JLabel badgeQueue = BrutalUI.createBadge("Hàng đợi 60s", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);

        matchHeader.add(lblMatchTitle, BorderLayout.WEST);
        matchHeader.add(badgeQueue, BorderLayout.EAST);
        matchCard.add(matchHeader, BorderLayout.NORTH);

        // Body Match Card: 2 Selector (Chủ đề & Độ khó) + CTA Button
        JPanel matchBody = new JPanel();
        matchBody.setOpaque(false);
        matchBody.setLayout(new BoxLayout(matchBody, BoxLayout.Y_AXIS));

        // Selector Chủ Đề
        JLabel lblTopicTitle = new JLabel("📚 CHỌN CHỦ ĐỀ (TOPIC)");
        lblTopicTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTopicTitle.setForeground(new Color(63, 63, 70));
        lblTopicTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbTopics = new JComboBox<>(new TopicItem[]{
                new TopicItem(0, "-- Tất cả chủ đề ngẫu nhiên --"),
                new TopicItem(1, "Toán học & Logic"),
                new TopicItem(2, "Lịch sử & Địa lý"),
                new TopicItem(3, "Khoa học Tự nhiên"),
                new TopicItem(4, "Tin học & Công nghệ")
        });
        styleComboBox(cbTopics);
        cbTopics.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Selector Độ Khó
        JLabel lblDiffTitle = new JLabel("🎯 CHỌN ĐỘ KHÓ (DIFFICULTY)");
        lblDiffTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDiffTitle.setForeground(new Color(63, 63, 70));
        lblDiffTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbDifficulty = new JComboBox<>(new String[]{"Mọi cấp độ (Mặc định)", "Dễ (Dành cho tân binh)", "Trung bình (Chuẩn thi đấu)", "Khó (Cao thủ tranh tài)"});
        styleComboBox(cbDifficulty);
        cbDifficulty.setSelectedIndex(2); // Mặc định là Trung bình
        cbDifficulty.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút Tìm Trận CTA Đậm Chất Brutal
        btnFindMatch = new BrutalUI.BrutalButton("⚡ BẮT ĐẦU TÌM ĐỐI THỦ NGAY (60S)", BrutalUI.BrutalButton.Style.PRIMARY);
        btnFindMatch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFindMatch.setPreferredSize(new Dimension(0, 48));
        btnFindMatch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnFindMatch.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFindMatch.addActionListener(e -> startFindingMatch());

        matchBody.add(lblTopicTitle);
        matchBody.add(Box.createVerticalStrut(4));
        matchBody.add(cbTopics);
        matchBody.add(Box.createVerticalStrut(14));
        matchBody.add(lblDiffTitle);
        matchBody.add(Box.createVerticalStrut(4));
        matchBody.add(cbDifficulty);
        matchBody.add(Box.createVerticalStrut(18));
        matchBody.add(btnFindMatch);
        matchBody.add(Box.createVerticalStrut(18));

        // Quick Stats Banner (3 thẻ mini viền đen)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStatTotal = new JLabel("0", SwingConstants.CENTER);
        lblStatWinRate = new JLabel("0%", SwingConstants.CENTER);
        lblStatIq = new JLabel("--", SwingConstants.CENTER);

        statsPanel.add(createMiniStatCard("TỔNG TRẬN", lblStatTotal, BrutalUI.COLOR_BLACK));
        statsPanel.add(createMiniStatCard("TỈ LỆ THẮNG", lblStatWinRate, new Color(22, 163, 74)));
        statsPanel.add(createMiniStatCard("ĐIỂM IQ", lblStatIq, new Color(37, 99, 235)));

        matchBody.add(statsPanel);
        matchCard.add(matchBody, BorderLayout.CENTER);

        centerPanel.add(matchCard);

        // --- CỘT PHẢI: Danh Sách Online & Thách Đấu Bạn Bè (Yêu cầu 3) ---
        BrutalUI.CardPanel onlineCard = new BrutalUI.CardPanel(16, 4);
        onlineCard.setLayout(new BorderLayout(14, 14));
        onlineCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Header Online Card
        JPanel onlineHeader = new JPanel(new BorderLayout(10, 0));
        onlineHeader.setOpaque(false);

        JPanel onlineTitleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        onlineTitleGroup.setOpaque(false);
        JLabel dotOnline = new JLabel("●");
        dotOnline.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dotOnline.setForeground(new Color(22, 163, 74));
        JLabel lblOnlineTitle = new JLabel("Người Chơi Online (Thách Đấu)");
        lblOnlineTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblOnlineTitle.setForeground(BrutalUI.COLOR_BLACK);
        onlineTitleGroup.add(dotOnline);
        onlineTitleGroup.add(lblOnlineTitle);

        BrutalUI.BrutalButton btnRefreshOnline = new BrutalUI.BrutalButton("Làm mới", BrutalUI.BrutalButton.Style.OUTLINE);
        btnRefreshOnline.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefreshOnline.addActionListener(e -> authService.requestOnlineUsers());

        onlineHeader.add(onlineTitleGroup, BorderLayout.WEST);
        onlineHeader.add(btnRefreshOnline, BorderLayout.EAST);
        onlineCard.add(onlineHeader, BorderLayout.NORTH);

        // Bảng danh sách người chơi online
        modelOnlineUsers = new DefaultTableModel(new String[]{"ID", "Tên người chơi", "Điểm IQ", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOnlineUsers = new JTable(modelOnlineUsers);
        tblOnlineUsers.setRowHeight(32);
        tblOnlineUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOnlineUsers.setSelectionBackground(BrutalUI.COLOR_SELECTED_BG);
        tblOnlineUsers.setSelectionForeground(BrutalUI.COLOR_BLACK);
        tblOnlineUsers.setGridColor(new Color(228, 228, 231));

        JTableHeader onlineHeaderTbl = tblOnlineUsers.getTableHeader();
        onlineHeaderTbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        onlineHeaderTbl.setBackground(BrutalUI.COLOR_LIGHT_GRAY);
        onlineHeaderTbl.setForeground(BrutalUI.COLOR_BLACK);
        onlineHeaderTbl.setPreferredSize(new Dimension(0, 32));
        onlineHeaderTbl.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        tblOnlineUsers.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblOnlineUsers.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        tblOnlineUsers.getColumnModel().getColumn(3).setCellRenderer(centerRender);

        JScrollPane scrollOnline = new JScrollPane(tblOnlineUsers);
        scrollOnline.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));
        onlineCard.add(scrollOnline, BorderLayout.CENTER);

        // Footer Online Card: Nút Mời Thách Đấu
        btnChallenge = new BrutalUI.BrutalButton("⚡ MỜI THÁCH ĐẤU NGƯỜI NÀY", BrutalUI.BrutalButton.Style.PRIMARY);
        btnChallenge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChallenge.setPreferredSize(new Dimension(0, 44));
        btnChallenge.addActionListener(e -> handleSendChallenge());
        onlineCard.add(btnChallenge, BorderLayout.SOUTH);

        centerPanel.add(onlineCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.setBackground(Color.WHITE);
        box.setForeground(BrutalUI.COLOR_BLACK);
        box.setPreferredSize(new Dimension(0, 38));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private JPanel createMiniStatCard(String title, JLabel valLabel, Color valColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 6, 8, 6)
        ));

        JLabel lblT = new JLabel(title, SwingConstants.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblT.setForeground(BrutalUI.COLOR_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valLabel.setForeground(valColor);

        card.add(lblT, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // LOGIC GHÉP TRẬN VÀ THÁCH ĐẤU (YÊU CẦU 2, 3, 4)
    // =========================================================================

    private String mapDifficulty(String raw) {
        if (raw == null) return "MEDIUM";
        if (raw.contains("Dễ") || raw.equalsIgnoreCase("EASY")) return "EASY";
        if (raw.contains("Khó") || raw.equalsIgnoreCase("HARD")) return "HARD";
        if (raw.contains("Mọi cấp độ") || raw.equalsIgnoreCase("ALL")) return "ALL";
        return "MEDIUM";
    }

    private void startFindingMatch() {
        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = mapDifficulty((String) cbDifficulty.getSelectedItem());

        // Gửi gói tin QUEUE_JOIN_REQ lên server
        matchmakingService.joinQueue(topicId, difficulty);

        // Chuyển sang màn hình chờ ghép cặp 60s
        mainFrame.getWaitingPanel().startCountdown(60);
        mainFrame.showPanel(MainClientFrame.CARD_WAITING);
    }

    private void handleSendChallenge() {
        int selectedRow = tblOnlineUsers.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi online từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long targetUserId = Long.parseLong(modelOnlineUsers.getValueAt(selectedRow, 0).toString());
        String targetUsername = modelOnlineUsers.getValueAt(selectedRow, 1).toString();

        if (currentUserId != null && currentUserId == targetUserId) {
            JOptionPane.showMessageDialog(this, "Bạn không thể tự thách đấu chính mình!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = mapDifficulty((String) cbDifficulty.getSelectedItem());

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

    public void showIncomingChallengeDialog(ChallengeDTO.InviteNotify invite) {
        String topicDesc = "Tất cả chủ đề";
        if (invite.topicId != null && invite.topicId > 0) {
            for (int i = 0; i < cbTopics.getItemCount(); i++) {
                TopicItem item = cbTopics.getItemAt(i);
                if (item != null && item.id == invite.topicId) {
                    topicDesc = item.name;
                    break;
                }
            }
        }
        String diffDesc = invite.difficulty != null ? invite.difficulty : "Trung bình";
        if ("EASY".equalsIgnoreCase(diffDesc)) diffDesc = "Dễ";
        else if ("HARD".equalsIgnoreCase(diffDesc)) diffDesc = "Khó";
        else if ("MEDIUM".equalsIgnoreCase(diffDesc)) diffDesc = "Trung bình";

        String displayName = (invite.fromFullName != null && !invite.fromFullName.trim().isEmpty())
                ? invite.fromFullName
                : invite.fromUsername;
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Người chơi [" + displayName + "] đang thách đấu 1v1 với bạn!\n• Chủ đề: " + topicDesc + "\n• Độ khó: " + diffDesc + "\n\nBạn có chấp nhận thi đấu ngay không?",
                "Lời Mời Thách Đấu 1v1 (YC 3)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            challengeService.acceptChallenge(invite.invitationId);
        } else {
            challengeService.rejectChallenge(invite.invitationId);
        }
    }

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
        lblUserStats.setText("🏆 Điểm IQ: " + (auth.iqScore != null ? auth.iqScore : "Chưa test") + " | Online");
        if (lblStatIq != null) {
            lblStatIq.setText(auth.iqScore != null ? String.valueOf(auth.iqScore.intValue()) : "--");
        }
        authService.requestOnlineUsers();
        matchmakingService.requestTopicList();
    }

    public void updateIQDisplay(double newIq) {
        lblUserStats.setText("🏆 Điểm IQ: " + newIq);
        if (lblStatIq != null) {
            lblStatIq.setText(String.valueOf((int) newIq));
        }
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void updateTopicList(List<com.quizzlive.shared.dto.TopicDTO> topics) {
        if (topics == null || topics.isEmpty()) return;
        cbTopics.removeAllItems();
        cbTopics.addItem(new TopicItem(0, "-- Tất cả chủ đề ngẫu nhiên --"));
        for (com.quizzlive.shared.dto.TopicDTO t : topics) {
            cbTopics.addItem(new TopicItem(t.topicId, t.topicName));
        }
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
