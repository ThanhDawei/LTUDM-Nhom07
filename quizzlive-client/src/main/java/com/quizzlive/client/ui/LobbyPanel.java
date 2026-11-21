package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientAuthService;
import com.quizzlive.client.service.ClientChallengeService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.AvatarUtil;
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
    private JLabel lblBio;
    private JLabel lblUserStats;
    private JLabel lblAvatarBadge;
    private String currentAvatar = "avatar1.png";
    private String currentBio = "";
    private JComboBox<TopicItem> cbTopics;
    private JComboBox<String> cbDifficulty;
    private JTable tblOnlineUsers;
    private DefaultTableModel modelOnlineUsers;
    private BrutalUI.BrutalButton btnFindMatch;
    private BrutalUI.BrutalButton btnChallenge;

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

        // Brand & User Profile Info (Neo-Brutalist Header)
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

        // Avatar Badge (Clickable - Yêu cầu 1)
        lblAvatarBadge = new JLabel(AvatarUtil.getAvatarIcon("avatar1.png", 42));
        lblAvatarBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAvatarBadge.setToolTipText("Bấm vào đây để cập nhật hồ sơ & ảnh đại diện");
        lblAvatarBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showUpdateProfileDialog();
            }
        });
        brandAndUserBox.add(lblAvatarBadge);

        JPanel userTextBox = new JPanel(new GridLayout(3, 1, 0, 1));
        userTextBox.setOpaque(false);

        lblWelcome = new JLabel("Xin chào, Người chơi!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWelcome.setForeground(BrutalUI.COLOR_BLACK);

        lblBio = new JLabel("Thành viên Quizzlive");
        lblBio.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblBio.setForeground(BrutalUI.COLOR_MUTED);

        lblUserStats = new JLabel("Điểm IQ: -- | Thắng: 0 | Chuỗi thắng: 0");
        lblUserStats.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUserStats.setForeground(BrutalUI.COLOR_MUTED);

        userTextBox.add(lblWelcome);
        userTextBox.add(lblBio);
        userTextBox.add(lblUserStats);
        brandAndUserBox.add(userTextBox);

        topPanel.add(brandAndUserBox, BorderLayout.WEST);

        // Action Navigation Buttons (Vừa vặn, căn giữa theo chiều dọc)
        JPanel navActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navActions.setOpaque(false);

        BrutalUI.BrutalButton btnLeaderboard = new BrutalUI.BrutalButton("Bảng Xếp Hạng", BrutalUI.BrutalButton.Style.OUTLINE);
        btnLeaderboard.addActionListener(e -> {
            mainFrame.getLeaderboardPanel().updateAvatar(currentAvatar);
            mainFrame.getLeaderboardPanel().refreshData(currentUserId);
            mainFrame.showPanel(MainClientFrame.CARD_LEADERBOARD);
        });

        BrutalUI.BrutalButton btnTournament = new BrutalUI.BrutalButton("Giải Đấu", BrutalUI.BrutalButton.Style.OUTLINE);
        btnTournament.addActionListener(e -> {
            new com.quizzlive.client.service.ClientTournamentService().requestTournamentList();
            mainFrame.showPanel(MainClientFrame.CARD_TOURNAMENT);
        });

        BrutalUI.BrutalButton btnTestIQ = new BrutalUI.BrutalButton("Test IQ", BrutalUI.BrutalButton.Style.OUTLINE);
        btnTestIQ.setToolTipText("Làm bài Test IQ hàng ngày (5 câu/ngày, kết toán sau 5 câu)");
        btnTestIQ.addActionListener(e -> showIQTestDialog());

        BrutalUI.BrutalButton btnProfile = new BrutalUI.BrutalButton("Hồ Sơ", BrutalUI.BrutalButton.Style.OUTLINE);
        btnProfile.addActionListener(e -> showUpdateProfileDialog());

        BrutalUI.BrutalButton btnLogout = new BrutalUI.BrutalButton("Đăng Xuất", BrutalUI.BrutalButton.Style.DANGER);
        btnLogout.addActionListener(e -> {
            authService.sendLogout();
            mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
        });

        BrutalUI.BrutalButton[] navBtns = {btnLeaderboard, btnTournament, btnTestIQ, btnProfile, btnLogout};
        Font navFont = new Font("Segoe UI", Font.BOLD, 12);
        for (BrutalUI.BrutalButton btn : navBtns) {
            btn.setFont(navFont);
            Dimension pref = btn.getPreferredSize();
            btn.setPreferredSize(new Dimension(pref.width + 8, 34));
            navActions.add(btn);
        }

        JPanel navCenterWrapper = new JPanel(new GridBagLayout());
        navCenterWrapper.setOpaque(false);
        navCenterWrapper.add(navActions);
        topPanel.add(navCenterWrapper, BorderLayout.EAST);

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
        JLabel lblTopicTitle = new JLabel("CHỌN CHỦ ĐỀ (TOPIC)");
        lblTopicTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTopicTitle.setForeground(new Color(63, 63, 70));
        lblTopicTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbTopics = new JComboBox<>(new TopicItem[]{
                new TopicItem(0, "-- Tất cả chủ đề ngẫu nhiên --"),
                new TopicItem(1, "Khoa học Tự nhiên"),
                new TopicItem(2, "Lịch sử & Địa lý"),
                new TopicItem(3, "Công nghệ & Máy tính"),
                new TopicItem(4, "Tư duy IQ & Logic")
        });
        styleComboBox(cbTopics);
        cbTopics.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Selector Độ Khó (Neo-Brutalist)
        JLabel lblDiffTitle = new JLabel("CHỌN ĐỘ KHÓ (DIFFICULTY)");
        lblDiffTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDiffTitle.setForeground(new Color(63, 63, 70));
        lblDiffTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbDifficulty = new JComboBox<>(new String[]{"-- Mọi cấp độ (ALL) --", "Dễ (EASY)", "Trung bình (MEDIUM)", "Khó (HARD)"});
        styleComboBox(cbDifficulty);
        cbDifficulty.setSelectedIndex(2); // Mặc định là Trung bình
        cbDifficulty.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút Tìm Trận CTA Đậm Chất Brutal
        btnFindMatch = new BrutalUI.BrutalButton("BẮT ĐẦU TÌM ĐỐI THỦ NGAY (60S)", BrutalUI.BrutalButton.Style.PRIMARY);
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
        matchBody.add(Box.createVerticalStrut(24));
        matchBody.add(btnFindMatch);

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
        tblOnlineUsers.setRowHeight(36);
        tblOnlineUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOnlineUsers.setSelectionBackground(BrutalUI.COLOR_SELECTED_BG);
        tblOnlineUsers.setSelectionForeground(BrutalUI.COLOR_BLACK);
        tblOnlineUsers.setGridColor(new Color(228, 228, 231));
        tblOnlineUsers.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblOnlineUsers.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblOnlineUsers.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblOnlineUsers.getColumnModel().getColumn(3).setPreferredWidth(75);
        tblOnlineUsers.getColumnModel().getColumn(4).setPreferredWidth(85);
        tblOnlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        btnChallenge = new BrutalUI.BrutalButton("MỜI THÁCH ĐẤU NGƯỜI NÀY", BrutalUI.BrutalButton.Style.PRIMARY);
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

    // =========================================================================
    // LOGIC GHÉP TRẬN VÀ THÁCH ĐẤU (YÊU CẦU 2, 3, 4)
    // =========================================================================

    private String mapDifficulty(String raw) {
        if (raw == null) return "ALL";
        String upper = raw.toUpperCase();
        if (upper.contains("EASY") || upper.contains("DỄ")) return "EASY";
        if (upper.contains("HARD") || upper.contains("KHÓ")) return "HARD";
        if (upper.contains("MEDIUM") || upper.contains("TRUNG BÌNH")) return "MEDIUM";
        if (upper.contains("ALL") || upper.contains("MỌI")) return "ALL";
        return "MEDIUM";
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
        mainFrame.getWaitingPanel().startCountdown(60, topicName, diffName, topicId, difficulty);
        mainFrame.showPanel(MainClientFrame.CARD_WAITING);
    }

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
                "Xác Nhận Thách Đấu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            challengeService.sendChallenge(targetUserId, topicId, difficulty);
            JOptionPane.showMessageDialog(this, "Đã gửi lời mời thách đấu tới " + targetName + "!\nVui lòng chờ đối thủ phản hồi (trong tối đa 60 giây).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
                "LỜI MỜI THÁCH ĐẤU 1V1",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            challengeService.acceptChallenge(invite.invitationId);
        } else {
            challengeService.rejectChallenge(invite.invitationId);
        }
    }


    private void styleBrutalField(JTextField field) {
        field.setFont(BrutalUI.FONT_BODY);
        field.setBackground(Color.WHITE);
        field.setForeground(BrutalUI.COLOR_BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void showUpdateProfileDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Cập Nhật Hồ Sơ Cá Nhân", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 500);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BrutalUI.COLOR_BG);
        dialog.setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        BrutalUI.CardPanel card = new BrutalUI.CardPanel(16, 4);
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("HỒ SƠ CÁ NHÂN");
        lblTitle.setFont(BrutalUI.FONT_TITLE_XL);
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);
        JLabel lblSub = new JLabel("Cập nhật tên hiển thị, ảnh đại diện và bảo mật tài khoản");
        lblSub.setFont(BrutalUI.FONT_SMALL);
        lblSub.setForeground(BrutalUI.COLOR_MUTED);
        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        content.add(headerPanel, BorderLayout.NORTH);

        // Form body
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        final String[] selectedAvatar = new String[]{currentAvatar != null ? currentAvatar : "avatar1.png"};

        // Avatar Box
        JPanel avatarBox = new JPanel(new BorderLayout(14, 0));
        avatarBox.setBackground(new Color(250, 250, 250));
        avatarBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblAvatarPreview = new JLabel(AvatarUtil.getAvatarIcon(selectedAvatar[0], 56));
        lblAvatarPreview.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));

        JPanel avatarMeta = new JPanel(new BorderLayout(0, 6));
        avatarMeta.setOpaque(false);
        JLabel lblAvatarName = new JLabel(getFileNameFromPath(selectedAvatar[0]));
        lblAvatarName.setFont(BrutalUI.FONT_SMALL_BOLD);
        lblAvatarName.setForeground(BrutalUI.COLOR_BLACK);

        BrutalUI.BrutalButton btnChooseAvatar = new BrutalUI.BrutalButton("Chọn ảnh từ máy...", BrutalUI.BrutalButton.Style.OUTLINE);
        btnChooseAvatar.setFont(BrutalUI.FONT_SMALL_BOLD);
        btnChooseAvatar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn ảnh đại diện mới từ máy tính");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
            int ret = chooser.showOpenDialog(dialog);
            if (ret == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                selectedAvatar[0] = AvatarUtil.saveCustomAvatar(file, "avatar_user_" + (currentUserId != null ? currentUserId : "0"));
                lblAvatarName.setText(file.getName());
                lblAvatarPreview.setIcon(AvatarUtil.getAvatarIcon(selectedAvatar[0], 56));
            }
        });

        avatarMeta.add(lblAvatarName, BorderLayout.NORTH);
        avatarMeta.add(btnChooseAvatar, BorderLayout.WEST);
        avatarBox.add(lblAvatarPreview, BorderLayout.WEST);
        avatarBox.add(avatarMeta, BorderLayout.CENTER);

        JTextField txtName = new JTextField(20);
        txtName.setText(lblWelcome.getText().replace("Xin chào, ", "").replace("!", "").trim());
        styleBrutalField(txtName);

        JTextField txtBio = new JTextField(20);
        txtBio.setText(currentBio != null ? currentBio : "");
        styleBrutalField(txtBio);

        BrutalUI.BrutalButton btnChangePass = new BrutalUI.BrutalButton("Đổi mật khẩu...", BrutalUI.BrutalButton.Style.OUTLINE);
        btnChangePass.setFont(BrutalUI.FONT_SMALL_BOLD);
        btnChangePass.addActionListener(e -> showChangePasswordDialog(dialog));

        int r = 0;
        gbc.gridy = r++; form.add(avatarBox, gbc);

        JLabel lName = new JLabel("Họ tên mới:");
        lName.setFont(BrutalUI.FONT_SMALL_BOLD);
        gbc.gridy = r++; gbc.insets = new Insets(10, 0, 3, 0); form.add(lName, gbc);
        gbc.gridy = r++; gbc.insets = new Insets(0, 0, 6, 0); form.add(txtName, gbc);

        JLabel lBio = new JLabel("Giới thiệu bản thân:");
        lBio.setFont(BrutalUI.FONT_SMALL_BOLD);
        gbc.gridy = r++; gbc.insets = new Insets(6, 0, 3, 0); form.add(lBio, gbc);
        gbc.gridy = r++; gbc.insets = new Insets(0, 0, 10, 0); form.add(txtBio, gbc);

        JPanel secRow = new JPanel(new BorderLayout(10, 0));
        secRow.setOpaque(false);
        JLabel lSec = new JLabel("Bảo mật tài khoản:");
        lSec.setFont(BrutalUI.FONT_SMALL_BOLD);
        secRow.add(lSec, BorderLayout.WEST);
        secRow.add(btnChangePass, BorderLayout.EAST);
        gbc.gridy = r++; form.add(secRow, gbc);

        content.add(form, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        BrutalUI.BrutalButton btnCancel = new BrutalUI.BrutalButton("Hủy", BrutalUI.BrutalButton.Style.OUTLINE);
        btnCancel.addActionListener(e -> dialog.dispose());

        BrutalUI.BrutalButton btnSave = new BrutalUI.BrutalButton("Lưu thay đổi", BrutalUI.BrutalButton.Style.PRIMARY);
        btnSave.addActionListener(e -> {
            String bioText = txtBio.getText().trim();
            authService.sendUpdateProfile(
                    txtName.getText().trim(),
                    null,
                    null,
                    selectedAvatar[0],
                    bioText
            );
            dialog.dispose();
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        content.add(footer, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        dialog.add(wrapper, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * Hộp thoại đổi mật khẩu riêng biệt (Yêu cầu 1)
     */
    private void showChangePasswordDialog(Window owner) {
        JDialog dialog = new JDialog(owner, "Đổi Mật Khẩu", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 380);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.getContentPane().setBackground(BrutalUI.COLOR_BG);
        dialog.setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        BrutalUI.CardPanel card = new BrutalUI.CardPanel(16, 4);
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lblTitle = new JLabel("ĐỔI MẬT KHẨU");
        lblTitle.setFont(BrutalUI.FONT_TITLE);
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);
        content.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 1, 0, 5));
        form.setOpaque(false);

        JPasswordField txtCurPass = new JPasswordField(18);
        JPasswordField txtNewPass = new JPasswordField(18);
        JPasswordField txtConfirmPass = new JPasswordField(18);
        styleBrutalField(txtCurPass);
        styleBrutalField(txtNewPass);
        styleBrutalField(txtConfirmPass);

        JLabel l1 = new JLabel("Mật khẩu hiện tại:");
        l1.setFont(BrutalUI.FONT_SMALL_BOLD);
        JLabel l2 = new JLabel("Mật khẩu mới:");
        l2.setFont(BrutalUI.FONT_SMALL_BOLD);
        JLabel l3 = new JLabel("Xác nhận mật khẩu mới:");
        l3.setFont(BrutalUI.FONT_SMALL_BOLD);

        form.add(l1); form.add(txtCurPass);
        form.add(l2); form.add(txtNewPass);
        form.add(l3); form.add(txtConfirmPass);
        content.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        BrutalUI.BrutalButton btnCancel = new BrutalUI.BrutalButton("Hủy", BrutalUI.BrutalButton.Style.OUTLINE);
        btnCancel.addActionListener(e -> dialog.dispose());

        BrutalUI.BrutalButton btnConfirm = new BrutalUI.BrutalButton("Xác nhận", BrutalUI.BrutalButton.Style.PRIMARY);
        btnConfirm.addActionListener(e -> {
            String curPass = new String(txtCurPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (curPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mật khẩu hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mật khẩu mới!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu mới phải có tối thiểu 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            authService.sendUpdateProfile(null, curPass, newPass, null, null);
            dialog.dispose();
        });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        content.add(footer, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        dialog.add(wrapper, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private String getFileNameFromPath(String path) {
        if (path == null || path.isEmpty()) return "Mặc định (avatar1.png)";
        int idx = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return idx >= 0 ? path.substring(idx + 1) : path;
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
        if (this.currentBio != null && !this.currentBio.trim().isEmpty()) {
            lblBio.setText("“" + this.currentBio + "”");
        } else {
            lblBio.setText("Thành viên Quizzlive");
        }
        lblUserStats.setText("Điểm IQ: " + (auth.iqScore != null ? auth.iqScore.intValue() : "--") + " | Online");
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
        cbTopics.addItem(new TopicItem(0, "-- Tất cả chủ đề ngẫu nhiên --"));
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
                        "Online"
                });
            }
        }
    }

    public void showIQTestDialog() {
        IQTestDialog dlg = new IQTestDialog(mainFrame);
        dlg.setVisible(true);
    }
}
