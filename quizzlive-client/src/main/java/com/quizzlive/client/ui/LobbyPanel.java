package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.service.ClientChallengeService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.AvatarUtil;
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
    private JLabel lblBio;
    private JLabel lblUserStats;
    private JLabel lblAvatarBadge;
    private String currentAvatar = "avatar1.png";
    private String currentBio = "";
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

        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profileHeader.setOpaque(false);

        lblAvatarBadge = new JLabel(AvatarUtil.getAvatarIcon("avatar1.png", 64));
        lblAvatarBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAvatarBadge.setToolTipText("Bấm vào đây để cập nhật hồ sơ & ảnh đại diện");
        lblAvatarBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showUpdateProfileDialog();
            }
        });

        JPanel userBox = new JPanel(new GridLayout(3, 1, 2, 2));
        userBox.setOpaque(false);

        lblWelcome = new JLabel("Xin chào, Người chơi!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblWelcome.setForeground(new Color(41, 128, 185));

        lblBio = new JLabel("Giới thiệu: Đang tải...");
        lblBio.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblBio.setForeground(Color.GRAY);

        lblUserStats = new JLabel("Điểm IQ: -- | Thắng: 0 | Chuỗi thắng: 0");
        lblUserStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserStats.setForeground(Color.DARK_GRAY);

        userBox.add(lblWelcome);
        userBox.add(lblBio);
        userBox.add(lblUserStats);

        profileHeader.add(lblAvatarBadge);
        profileHeader.add(userBox);
        topPanel.add(profileHeader, BorderLayout.WEST);

        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navActions.setOpaque(false);

        JButton btnLeaderboard = new JButton("🏆 Bảng Xếp Hạng");
        btnLeaderboard.setFocusPainted(false);
        btnLeaderboard.addActionListener(e -> {
            mainFrame.getLeaderboardPanel().refreshData(currentUserId);
            mainFrame.showPanel(MainClientFrame.CARD_LEADERBOARD);
        });

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
                new TopicItem(1, "Khoa học Tự nhiên"),
                new TopicItem(2, "Lịch sử & Địa lý"),
                new TopicItem(3, "Công nghệ & Máy tính"),
                new TopicItem(4, "Tư duy IQ & Logic")
        });
        matchCard.add(cbTopics, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        matchCard.add(new JLabel("Độ khó (YC 4):"), gbc);
        gbc.gridx = 1;
        cbDifficulty = new JComboBox<>(new String[]{
                "-- Mọi cấp độ (ALL) --",
                "Dễ (EASY)",
                "Trung bình (MEDIUM)",
                "Khó (HARD)"
        });
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

        modelOnlineUsers = new DefaultTableModel(new String[]{"ID", "Ảnh", "Tên người chơi", "Điểm IQ", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return ImageIcon.class;
                return Object.class;
            }
        };
        tblOnlineUsers = new JTable(modelOnlineUsers);
        tblOnlineUsers.setRowHeight(34);
        tblOnlineUsers.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblOnlineUsers.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblOnlineUsers.getColumnModel().getColumn(2).setPreferredWidth(170);
        tblOnlineUsers.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblOnlineUsers.getColumnModel().getColumn(4).setPreferredWidth(85);
        tblOnlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblOnlineUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleSendChallenge();
                }
            }
        });
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
    private String mapDifficulty(String raw) {
        if (raw == null) return "ALL";
        String upper = raw.toUpperCase();
        if (upper.contains("EASY") || upper.contains("DỄ")) return "EASY";
        if (upper.contains("HARD") || upper.contains("KHÓ")) return "HARD";
        if (upper.contains("MEDIUM") || upper.contains("TRUNG BÌNH")) return "MEDIUM";
        if (upper.contains("ALL") || upper.contains("MỌI")) return "ALL";
        return "ALL";
    }

    private void startFindingMatch() {
        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = mapDifficulty((String) cbDifficulty.getSelectedItem());

        // Gửi gói tin QUEUE_JOIN_REQ lên server
        matchmakingService.joinQueue(topicId, difficulty);

        String topicName = selectedTopic != null ? selectedTopic.name : "Tất cả chủ đề";
        String diffName = cbDifficulty != null ? (String) cbDifficulty.getSelectedItem() : "Mọi cấp độ";

        // Chuyển sang màn hình chờ ghép cặp 60s
        mainFrame.getWaitingPanel().startCountdown(60, topicName, diffName);
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
        String targetName = modelOnlineUsers.getValueAt(selectedRow, 2).toString();

        if (currentUserId != null && currentUserId == targetUserId) {
            JOptionPane.showMessageDialog(this, "Bạn không thể tự thách đấu chính mình!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TopicItem selectedTopic = (TopicItem) cbTopics.getSelectedItem();
        Integer topicId = (selectedTopic == null || selectedTopic.id == 0) ? null : selectedTopic.id;
        String difficulty = mapDifficulty((String) cbDifficulty.getSelectedItem());

        String topicName = selectedTopic != null ? selectedTopic.name : "Tất cả chủ đề";
        String diffName = cbDifficulty != null ? (String) cbDifficulty.getSelectedItem() : "Mọi cấp độ";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn gửi lời mời thách đấu 1v1 tới [" + targetName + "]?\n" +
                "• Chủ đề câu hỏi: " + topicName + "\n" +
                "• Mức độ khó: " + diffName,
                "Xác Nhận Thách Đấu (Yêu Cầu 3)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            challengeService.sendChallenge(targetUserId, topicId, difficulty);
            JOptionPane.showMessageDialog(this, "Đã gửi lời mời thách đấu tới " + targetName + "!\nVui lòng chờ đối thủ phản hồi (trong tối đa 60 giây).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Popup khi nhận được lời mời thách đấu từ người khác (Yêu cầu 3)
     */
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
        else if ("ALL".equalsIgnoreCase(diffDesc)) diffDesc = "Mọi cấp độ";

        String displayName = (invite.fromFullName != null && !invite.fromFullName.trim().isEmpty())
                ? invite.fromFullName + " (" + invite.fromUsername + ")"
                : invite.fromUsername;

        ImageIcon invAvatar = AvatarUtil.getAvatarIcon(invite.fromAvatar, 52);
        JPanel inviteBox = new JPanel(new BorderLayout(15, 10));
        inviteBox.setOpaque(false);
        JLabel lblIcon = new JLabel(invAvatar);

        String iqInfo = (invite.fromIqScore != null && invite.fromIqScore > 0) ? (invite.fromIqScore + " IQ") : "Chưa test";
        JLabel lblMsg = new JLabel("<html><div style='font-family: Segoe UI;'>" +
                "<b style='font-size: 13px; color: #2980b9;'>" + displayName + "</b> vừa gửi lời mời thách đấu 1v1 tới bạn!<br><br>" +
                "• <b>Điểm IQ đối thủ:</b> " + iqInfo + "<br>" +
                "• <b>Chủ đề thi đấu:</b> " + topicDesc + "<br>" +
                "• <b>Độ khó câu hỏi:</b> " + diffDesc + "<br><br>" +
                "<span style='color: #27ae60;'>Bạn có chấp nhận vào trận ngay bây giờ không?</span>" +
                "</div></html>");

        inviteBox.add(lblIcon, BorderLayout.WEST);
        inviteBox.add(lblMsg, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
                this,
                inviteBox,
                "⚔️ LỜI MỜI THÁCH ĐẤU 1V1 (YÊU CẦU 3)",
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
     * Cập nhật thông tin cá nhân: Họ tên, Ảnh đại diện, Giới thiệu & Đổi mật khẩu (Yêu cầu 1)
     */
    private void showUpdateProfileDialog() {
        JTextField txtName = new JTextField(20);
        txtName.setText(lblWelcome.getText().replace("Xin chào, ", "").replace("!", "").trim());

        JComboBox<String> cbAvatar = new JComboBox<>(AvatarUtil.AVAILABLE_AVATARS);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String av = value.toString();
                    lbl.setIcon(AvatarUtil.getAvatarIcon(av, 28));
                    lbl.setText(" " + av);
                }
                return lbl;
            }
        });
        if (currentAvatar != null) {
            cbAvatar.setSelectedItem(currentAvatar);
        }

        JLabel lblAvatarPreview = new JLabel(AvatarUtil.getAvatarIcon(currentAvatar, 44));
        cbAvatar.addActionListener(e -> {
            String sel = (String) cbAvatar.getSelectedItem();
            lblAvatarPreview.setIcon(AvatarUtil.getAvatarIcon(sel, 44));
        });

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(cbAvatar);
        avatarRow.add(lblAvatarPreview);

        JTextField txtBio = new JTextField(20);
        txtBio.setText(currentBio != null ? currentBio : "");

        JPasswordField txtCurPass = new JPasswordField(20);
        JPasswordField txtNewPass = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("Họ tên mới:"));
        panel.add(txtName);
        panel.add(new JLabel("Ảnh đại diện (avatar):"));
        panel.add(avatarRow);
        panel.add(new JLabel("Giới thiệu bản thân:"));
        panel.add(txtBio);
        panel.add(new JLabel("Mật khẩu hiện tại:"));
        panel.add(txtCurPass);
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(txtNewPass);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cập Nhật Hồ Sơ Cá Nhân (YC 1)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedAvatar = (String) cbAvatar.getSelectedItem();
            String bioText = txtBio.getText().trim();
            authService.sendUpdateProfile(
                    txtName.getText().trim(),
                    new String(txtCurPass.getPassword()),
                    new String(txtNewPass.getPassword()),
                    selectedAvatar,
                    bioText
            );
        }
    }

    private Timer onlineRefreshTimer;

    private void startOnlineRefreshTimer() {
        if (onlineRefreshTimer == null) {
            onlineRefreshTimer = new Timer(5000, e -> {
                if (isShowing() && currentUserId != null) {
                    authService.requestOnlineUsers();
                }
            });
            onlineRefreshTimer.start();
        }
    }

    public void updateUserInfo(AuthDTO.AuthResponse auth) {
        this.currentUserId = auth.userId;
        this.currentUsername = auth.username;
        this.currentAvatar = (auth.avatarUrl != null && !auth.avatarUrl.trim().isEmpty()) ? auth.avatarUrl.trim() : "avatar1.png";
        this.currentBio = (auth.bio != null && !auth.bio.trim().isEmpty()) ? auth.bio.trim() : "Thành viên Quizzlive";

        if (lblAvatarBadge != null) {
            lblAvatarBadge.setIcon(AvatarUtil.getAvatarIcon(this.currentAvatar, 64));
        }

        lblWelcome.setText("Xin chào, " + (auth.fullName != null ? auth.fullName : auth.username) + "!");
        lblBio.setText("“" + this.currentBio + "”");
        lblUserStats.setText("Điểm IQ: " + (auth.iqScore != null ? auth.iqScore : "Chưa test"));

        authService.requestOnlineUsers();
        matchmakingService.requestTopicList();
        startOnlineRefreshTimer();
    }

    public void updateIQDisplay(double newIq) {
        lblUserStats.setText("Điểm IQ: " + newIq);
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void updateTopicList(List<com.quizzlive.shared.dto.TopicDTO> topics) {
        if (topics == null || topics.isEmpty()) return;
        TopicItem prevSelected = (TopicItem) cbTopics.getSelectedItem();
        Integer prevId = prevSelected != null ? prevSelected.id : null;

        cbTopics.removeAllItems();
        cbTopics.addItem(new TopicItem(0, "-- Tất cả chủ đề --"));
        int selectIdx = 0;
        int idx = 1;
        for (com.quizzlive.shared.dto.TopicDTO t : topics) {
            cbTopics.addItem(new TopicItem(t.topicId, t.topicName));
            if (prevId != null && prevId == t.topicId) {
                selectIdx = idx;
            }
            idx++;
        }
        cbTopics.setSelectedIndex(selectIdx);
    }

    public void updateOnlineUsersList(List<AuthDTO.AuthResponse> users) {
        modelOnlineUsers.setRowCount(0);
        if (users != null) {
            for (AuthDTO.AuthResponse u : users) {
                if (currentUserId != null && u.userId != null && u.userId.equals(currentUserId)) {
                    continue; // Không hiển thị chính mình trong danh sách đối thủ online
                }
                String displayName = (u.fullName != null && !u.fullName.trim().isEmpty())
                        ? u.fullName + " (" + u.username + ")"
                        : u.username;
                ImageIcon avatarIcon = AvatarUtil.getAvatarIcon(u.avatarUrl, 26);
                modelOnlineUsers.addRow(new Object[]{
                        u.userId,
                        avatarIcon,
                        displayName,
                        (u.iqScore != null && u.iqScore > 0) ? u.iqScore : "Chưa test",
                        "🟢 Online"
                });
            }
        }
    }
}
