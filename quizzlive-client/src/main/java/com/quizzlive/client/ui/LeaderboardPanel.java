package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientLeaderboardService;
import com.quizzlive.client.util.AvatarUtil;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.shared.dto.LeaderboardDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Bảng Xếp Hạng & Lịch Sử Thi Đấu - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế theo SCREEN 10 trong master_gui_showcase.html
 * Phụ trách: Thành viên 1 · Yêu cầu 10 (Xếp hạng chung & Lịch sử trận đấu)
 */
public class LeaderboardPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientLeaderboardService leaderboardService = new ClientLeaderboardService();

    private JTabbedPane tabbedPane;

    // Banner Thông số cá nhân (My Stats Card)
    private BrutalUI.CardPanel pnlMyStats;
    private JLabel lblMyAvatar;
    private JLabel lblMyName;
    private JLabel lblMyRank;
    private JLabel lblMyWinRate;
    private JLabel lblMyStreaks;
    private JLabel lblMyScore;

    // Tab 1: Top Điểm & Thành Tích
    private DefaultTableModel modelTopPoints;
    private JTable tblTopPoints;

    // Tab 2: Top Chuỗi Thắng
    private DefaultTableModel modelTopStreaks;
    private JTable tblTopStreaks;

    // Tab 3: Lịch Sử Đấu Cá Nhân
    private DefaultTableModel modelHistory;
    private JTable tblHistory;
    private JLabel lblHistoryCount;

    public LeaderboardPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(14, 14));
        setBackground(BrutalUI.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // =========================================================================
        // 1. TOP HEADER (Neo-Brutalist Card)
        // =========================================================================
        BrutalUI.CardPanel headerCard = new BrutalUI.CardPanel(14, 3);
        headerCard.setLayout(new BorderLayout(12, 0));
        headerCard.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);

        JLabel badge = BrutalUI.createBadge("Yêu Cầu 10", new Color(244, 244, 245), BrutalUI.COLOR_BLACK);
        JLabel lblTitle = new JLabel("BẢNG XẾP HẠNG & LỊCH SỬ THI ĐẤU");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);

        headerLeft.add(lblTitle);
        headerLeft.add(badge);
        headerCard.add(headerLeft, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        BrutalUI.BrutalButton btnRefresh = new BrutalUI.BrutalButton("Làm Mới", BrutalUI.BrutalButton.Style.OUTLINE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> refreshData(mainFrame.getLobbyPanel().getCurrentUserId()));

        BrutalUI.BrutalButton btnBack = new BrutalUI.BrutalButton("Quay lại Phòng Chờ", BrutalUI.BrutalButton.Style.PRIMARY);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setPreferredSize(new Dimension(170, 36));
        btnBack.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOBBY));

        actions.add(btnRefresh);
        actions.add(btnBack);
        headerCard.add(actions, BorderLayout.EAST);

        // =========================================================================
        // 2. MY STATS BANNER CARD (Thành tích cá nhân của người chơi hiện tại)
        // =========================================================================
        pnlMyStats = createMyStatsCard();

        JPanel northPanel = new JPanel(new BorderLayout(0, 12));
        northPanel.setOpaque(false);
        northPanel.add(headerCard, BorderLayout.NORTH);
        northPanel.add(pnlMyStats, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // =========================================================================
        // 3. CENTER CARD: TABS BẢNG XẾP HẠNG VÀ LỊCH SỬ
        // =========================================================================
        BrutalUI.CardPanel centerCard = new BrutalUI.CardPanel(16, 4);
        centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Top Điểm & Thành Tích
        modelTopPoints = new DefaultTableModel(new String[]{
                "Thứ hạng", "Tên tài khoản", "Họ tên", "Tổng trận", "Thắng", "Thua", "Hòa", "Tỉ lệ thắng (%)", "Chuỗi hiện tại", "Tổng điểm", "Điểm IQ"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTopPoints = new JTable(modelTopPoints);
        styleBrutalTable(tblTopPoints);
        setupTableAlignment(tblTopPoints);
        tabbedPane.addTab("Top Điểm & Thành Tích", createTableScrollPane(tblTopPoints));

        // Tab 2: Top Chuỗi Thắng
        modelTopStreaks = new DefaultTableModel(new String[]{
                "Thứ hạng", "Tên tài khoản", "Họ tên", "Chuỗi hiện tại", "Chuỗi kỷ lục", "Tổng trận", "Tỉ lệ thắng (%)", "Tổng điểm"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTopStreaks = new JTable(modelTopStreaks);
        styleBrutalTable(tblTopStreaks);
        setupTableAlignment(tblTopStreaks);
        tabbedPane.addTab("Top Chuỗi Thắng Dài Nhất", createTableScrollPane(tblTopStreaks));

        // Tab 3: Lịch Sử Đấu Cá Nhân
        JPanel pnlHistoryTab = new JPanel(new BorderLayout(0, 10));
        pnlHistoryTab.setOpaque(false);
        pnlHistoryTab.setBorder(BorderFactory.createEmptyBorder(8, 6, 6, 6));

        lblHistoryCount = new JLabel("Lịch sử các trận đấu đã tham gia (10 trận gần nhất):");
        lblHistoryCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHistoryCount.setForeground(BrutalUI.COLOR_BLACK);
        pnlHistoryTab.add(lblHistoryCount, BorderLayout.NORTH);

        modelHistory = new DefaultTableModel(new String[]{
                "Mã trận", "Đối thủ", "Kết quả", "Điểm của bạn", "Điểm đối thủ", "Chủ đề", "Độ khó", "Thời gian"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistory = new JTable(modelHistory);
        styleBrutalTable(tblHistory);
        setupHistoryTableAlignment(tblHistory);
        pnlHistoryTab.add(createTableScrollPane(tblHistory), BorderLayout.CENTER);

        tabbedPane.addTab("Lịch Sử Đấu Cá Nhân", pnlHistoryTab);

        centerCard.add(tabbedPane, BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);
    }

    private BrutalUI.CardPanel createMyStatsCard() {
        BrutalUI.CardPanel card = new BrutalUI.CardPanel(14, 3);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Left: Avatar có viền đen
        lblMyAvatar = new JLabel(AvatarUtil.getAvatarIcon("avatar1.png", 52));
        lblMyAvatar.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));
        card.add(lblMyAvatar, BorderLayout.WEST);

        // Center: Stats grid
        JPanel pnlContent = new JPanel(new GridLayout(2, 1, 4, 6));
        pnlContent.setOpaque(false);

        JPanel pnlRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlRow1.setOpaque(false);

        lblMyName = new JLabel("Hồ sơ: Đang tải...");
        lblMyName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMyName.setForeground(BrutalUI.COLOR_BLACK);

        lblMyRank = BrutalUI.createBadge("Thứ hạng: #--", new Color(254, 243, 199), new Color(146, 64, 14));

        pnlRow1.add(lblMyName);
        pnlRow1.add(lblMyRank);

        JPanel pnlRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        pnlRow2.setOpaque(false);

        lblMyWinRate = new JLabel("Tỉ lệ thắng: --");
        lblMyWinRate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMyWinRate.setForeground(new Color(22, 163, 74));

        lblMyStreaks = new JLabel("Chuỗi thắng: --");
        lblMyStreaks.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMyStreaks.setForeground(new Color(217, 119, 6));

        lblMyScore = new JLabel("Tổng điểm: -- | Điểm IQ: --");
        lblMyScore.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMyScore.setForeground(new Color(37, 99, 235));

        pnlRow2.add(lblMyWinRate);
        pnlRow2.add(lblMyStreaks);
        pnlRow2.add(lblMyScore);

        pnlContent.add(pnlRow1);
        pnlContent.add(pnlRow2);

        card.add(pnlContent, BorderLayout.CENTER);
        return card;
    }

    private void styleBrutalTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(254, 243, 199)); // Pastel amber
        table.setSelectionForeground(BrutalUI.COLOR_BLACK);
        table.setGridColor(new Color(228, 228, 231));
        table.setShowGrid(true);

        // Header style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(BrutalUI.COLOR_BLACK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private void setupTableAlignment(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rankRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                if (row == 0) {
                    c.setForeground(new Color(217, 119, 6)); // Top 1 Gold Amber
                } else if (row == 1) {
                    c.setForeground(new Color(100, 116, 139)); // Top 2 Silver Slate
                } else if (row == 2) {
                    c.setForeground(new Color(180, 83, 9)); // Top 3 Bronze
                } else {
                    c.setForeground(BrutalUI.COLOR_BLACK);
                }
                return c;
            }
        };

        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(rankRenderer);
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
        }
        for (int i = 3; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void setupHistoryTableAlignment(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer resultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                String text = String.valueOf(value);
                if ("WIN".equalsIgnoreCase(text) || text.contains("THẮNG")) {
                    c.setText("THẮNG");
                    c.setForeground(new Color(22, 163, 74));
                } else if ("LOSS".equalsIgnoreCase(text) || text.contains("THUA")) {
                    c.setText("THUA");
                    c.setForeground(new Color(220, 38, 38));
                } else if ("DRAW".equalsIgnoreCase(text) || text.contains("HÒA")) {
                    c.setText("HÒA");
                    c.setForeground(new Color(217, 119, 6));
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(resultRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
    }

    public void renderLeaderboard(LeaderboardDTO.LeaderboardResponse resp) {
        SwingUtilities.invokeLater(() -> {
            modelTopPoints.setRowCount(0);
            modelTopStreaks.setRowCount(0);

            if (resp == null) return;

            // Cập nhật thẻ thành tích cá nhân (My Stats)
            if (resp.myStats != null) {
                LeaderboardDTO.LeaderboardItem me = resp.myStats;
                String displayTitle = me.fullName != null && !me.fullName.trim().isEmpty() ? me.fullName : me.username;
                lblMyName.setText(displayTitle + " (@" + me.username + ")");
                lblMyRank.setText("Thứ hạng: #" + me.rank);
                lblMyWinRate.setText(String.format("Tỉ lệ thắng: %.1f%% (%d Thắng - %d Thua - %d Hòa / %d trận)",
                        me.winRate, me.winMatches, me.loseMatches, me.drawMatches, me.totalMatches));
                lblMyStreaks.setText("Chuỗi thắng: Hiện tại " + me.currentStreak + " trận | Kỷ lục " + me.maxStreak + " trận");
                lblMyScore.setText(String.format("Tổng điểm: %,d điểm | Điểm IQ: %.1f", me.totalPoints, me.iqScore));
            } else {
                lblMyName.setText("Người chơi QuizzLive");
                lblMyRank.setText("Thứ hạng: #--");
                lblMyWinRate.setText("Tỉ lệ thắng: Chưa có dữ liệu");
                lblMyStreaks.setText("Chuỗi thắng: --");
                lblMyScore.setText("Tổng điểm: --");
            }

            // Tab 1: Top Điểm & Thắng
            if (resp.topPlayers != null) {
                for (LeaderboardDTO.LeaderboardItem item : resp.topPlayers) {
                    String rankDisplay = item.rank == 1 ? "Top 1" : (item.rank == 2 ? "Top 2" : (item.rank == 3 ? "Top 3" : "#" + item.rank));
                    modelTopPoints.addRow(new Object[]{
                            rankDisplay,
                            item.username,
                            item.fullName != null ? item.fullName : "--",
                            item.totalMatches,
                            item.winMatches,
                            item.loseMatches,
                            item.drawMatches,
                            String.format("%.1f%%", item.winRate),
                            item.currentStreak > 0 ? ("+" + item.currentStreak) : "0",
                            String.format("%,d", item.totalPoints),
                            String.format("%.1f", item.iqScore)
                    });
                }
            }

            // Tab 2: Top Chuỗi Thắng
            if (resp.topStreaks != null) {
                for (LeaderboardDTO.LeaderboardItem item : resp.topStreaks) {
                    String rankDisplay = item.rank == 1 ? "Top 1" : (item.rank == 2 ? "Top 2" : (item.rank == 3 ? "Top 3" : "#" + item.rank));
                    modelTopStreaks.addRow(new Object[]{
                            rankDisplay,
                            item.username,
                            item.fullName != null ? item.fullName : "--",
                            item.currentStreak > 0 ? ("+" + item.currentStreak) : "0",
                            item.maxStreak,
                            item.totalMatches,
                            String.format("%.1f%%", item.winRate),
                            String.format("%,d", item.totalPoints)
                    });
                }
            }
        });
    }

    public void refreshData(Long userId) {
        leaderboardService.requestLeaderboard(userId);
        if (userId != null && userId > 0) {
            leaderboardService.requestMatchHistory(userId);
        }
    }

    public void renderMatchHistory(List<LeaderboardDTO.MatchHistoryItem> history) {
        SwingUtilities.invokeLater(() -> {
            modelHistory.setRowCount(0);
            if (history != null) {
                lblHistoryCount.setText("Lịch sử thi đấu cá nhân: Đã hoàn thành " + history.size() + " trận gần nhất");
                for (LeaderboardDTO.MatchHistoryItem h : history) {
                    String opponentDisplay = h.opponentFullName != null && !h.opponentFullName.isEmpty()
                            ? (h.opponentFullName + " (" + h.opponentUsername + ")")
                            : h.opponentUsername;

                    modelHistory.addRow(new Object[]{
                            h.matchCode != null ? h.matchCode : ("MATCH_" + h.matchId),
                            opponentDisplay,
                            h.result != null ? h.result : "--",
                            h.myScore,
                            h.opponentScore,
                            h.topicName != null ? h.topicName : "Tất cả chủ đề",
                            h.difficulty != null ? h.difficulty : "MEDIUM",
                            h.playedAt != null ? h.playedAt : "--"
                    });
                }
            } else {
                lblHistoryCount.setText("Lịch sử thi đấu cá nhân: Chưa có trận đấu nào.");
            }
        });
    }

    public void updateAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            lblMyAvatar.setIcon(AvatarUtil.getAvatarIcon(avatarUrl, 52));
        }
    }
}
