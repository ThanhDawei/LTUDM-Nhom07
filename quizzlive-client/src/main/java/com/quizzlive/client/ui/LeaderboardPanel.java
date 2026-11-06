package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientLeaderboardService;
import com.quizzlive.client.util.AvatarUtil;
import com.quizzlive.shared.dto.LeaderboardDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Bảng Xếp Hạng & Lịch Sử Thi Đấu
 * Phụ trách Yêu cầu 10: "Xếp hạng chung: thứ hạng, chuỗi thắng/thua dài nhất, tỉ lệ thắng/thua; xem lịch sử trận"
 */
public class LeaderboardPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientLeaderboardService leaderboardService = new ClientLeaderboardService();

    private JTabbedPane tabbedPane;

    // Banner Thông số cá nhân (My Stats Card)
    private JPanel pnlMyStats;
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
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. TOP HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JLabel lblTitle = new JLabel("🏆 BẢNG XẾP HẠNG & LỊCH SỬ THI ĐẤU (Yêu cầu 10)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(41, 128, 185));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRefresh = new JButton("🔄 Làm Mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshData(mainFrame.getLobbyPanel().getCurrentUserId()));

        JButton btnBack = new JButton("⬅️ Quay lại Phòng Chờ");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOBBY));

        actions.add(btnRefresh);
        actions.add(btnBack);
        header.add(actions, BorderLayout.EAST);

        // 2. MY STATS BANNER CARD (Thành tích cá nhân của người chơi hiện tại)
        pnlMyStats = createMyStatsCard();

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(header, BorderLayout.NORTH);
        northPanel.add(pnlMyStats, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // 3. TABS
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
        tblTopPoints.setRowHeight(28);
        tblTopPoints.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTopPoints.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTopPoints.getTableHeader().setBackground(new Color(236, 240, 245));
        setupTableAlignment(tblTopPoints);
        tabbedPane.addTab("🥇 Bảng Xếp Hạng Điểm & Thắng", new JScrollPane(tblTopPoints));

        // Tab 2: Top Chuỗi Thắng
        modelTopStreaks = new DefaultTableModel(new String[]{
                "Thứ hạng", "Tên tài khoản", "Họ tên", "Chuỗi hiện tại", "Chuỗi kỷ lục", "Tổng trận", "Tỉ lệ thắng (%)", "Tổng điểm"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTopStreaks = new JTable(modelTopStreaks);
        tblTopStreaks.setRowHeight(28);
        tblTopStreaks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTopStreaks.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTopStreaks.getTableHeader().setBackground(new Color(236, 240, 245));
        setupTableAlignment(tblTopStreaks);
        tabbedPane.addTab("🔥 Top Chuỗi Thắng Dài Nhất", new JScrollPane(tblTopStreaks));

        // Tab 3: Lịch Sử Đấu Cá Nhân
        JPanel pnlHistoryTab = new JPanel(new BorderLayout(0, 8));
        pnlHistoryTab.setBackground(Color.WHITE);
        pnlHistoryTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblHistoryCount = new JLabel("Lịch sử các trận đấu đã tham gia:");
        lblHistoryCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHistoryCount.setForeground(new Color(52, 73, 94));
        pnlHistoryTab.add(lblHistoryCount, BorderLayout.NORTH);

        modelHistory = new DefaultTableModel(new String[]{
                "Mã trận", "Đối thủ", "Kết quả", "Điểm của bạn", "Điểm đối thủ", "Chủ đề", "Độ khó", "Thời gian"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(28);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblHistory.getTableHeader().setBackground(new Color(236, 240, 245));
        setupHistoryTableAlignment(tblHistory);
        pnlHistoryTab.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

        tabbedPane.addTab("📜 Lịch Sử Đấu Cá Nhân", pnlHistoryTab);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMyStatsCard() {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(new Color(238, 246, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 215, 245), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Left: Avatar
        lblMyAvatar = new JLabel(AvatarUtil.getAvatarIcon("avatar1.png", 54));
        card.add(lblMyAvatar, BorderLayout.WEST);

        // Center: Stats grid
        JPanel pnlContent = new JPanel(new GridLayout(2, 1, 4, 4));
        pnlContent.setOpaque(false);

        JPanel pnlRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlRow1.setOpaque(false);

        lblMyName = new JLabel("👤 Hồ sơ: Đang tải...");
        lblMyName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMyName.setForeground(new Color(41, 128, 185));

        lblMyRank = new JLabel("🏅 Thứ hạng: --");
        lblMyRank.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMyRank.setForeground(new Color(230, 126, 34));

        pnlRow1.add(lblMyName);
        pnlRow1.add(lblMyRank);

        JPanel pnlRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlRow2.setOpaque(false);

        lblMyWinRate = new JLabel("🎯 Tỉ lệ thắng: --");
        lblMyWinRate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblMyStreaks = new JLabel("🔥 Chuỗi thắng: --");
        lblMyStreaks.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblMyScore = new JLabel("💎 Tổng điểm: -- | Điểm IQ: --");
        lblMyScore.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlRow2.add(lblMyWinRate);
        pnlRow2.add(lblMyStreaks);
        pnlRow2.add(lblMyScore);

        pnlContent.add(pnlRow1);
        pnlContent.add(pnlRow2);

        card.add(pnlContent, BorderLayout.CENTER);
        return card;
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
                    c.setForeground(new Color(212, 175, 55)); // Gold
                } else if (row == 1) {
                    c.setForeground(new Color(149, 165, 166)); // Silver
                } else if (row == 2) {
                    c.setForeground(new Color(184, 115, 51)); // Bronze
                } else {
                    c.setForeground(Color.DARK_GRAY);
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
                    c.setText("🏆 THẮNG");
                    c.setForeground(new Color(39, 174, 96));
                } else if ("LOSS".equalsIgnoreCase(text) || text.contains("THUA")) {
                    c.setText("❌ THUA");
                    c.setForeground(new Color(231, 76, 60));
                } else if ("DRAW".equalsIgnoreCase(text) || text.contains("HÒA")) {
                    c.setText("🤝 HÒA");
                    c.setForeground(new Color(243, 156, 18));
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
                lblMyName.setText("👤 " + displayTitle + " (@" + me.username + ")");
                lblMyRank.setText("🏅 Thứ hạng: #" + me.rank);
                lblMyWinRate.setText(String.format("🎯 Tỉ lệ thắng: %.1f%% (%d Thắng - %d Thua - %d Hòa / %d trận)",
                        me.winRate, me.winMatches, me.loseMatches, me.drawMatches, me.totalMatches));
                lblMyStreaks.setText("🔥 Chuỗi thắng: Hiện tại " + me.currentStreak + " trận | Kỷ lục " + me.maxStreak + " trận");
                lblMyScore.setText(String.format("💎 Tổng điểm: %,d điểm | Điểm IQ: %.1f", me.totalPoints, me.iqScore));
            } else {
                lblMyName.setText("👤 Người chơi QuizzLive");
                lblMyRank.setText("🏅 Thứ hạng: #--");
                lblMyWinRate.setText("🎯 Tỉ lệ thắng: Chưa có dữ liệu");
                lblMyStreaks.setText("🔥 Chuỗi thắng: --");
                lblMyScore.setText("💎 Tổng điểm: --");
            }

            // Tab 1: Top Điểm & Thắng
            if (resp.topPlayers != null) {
                for (LeaderboardDTO.LeaderboardItem item : resp.topPlayers) {
                    String rankDisplay = item.rank == 1 ? "🥇 1" : (item.rank == 2 ? "🥈 2" : (item.rank == 3 ? "🥉 3" : "#" + item.rank));
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
                    String rankDisplay = item.rank == 1 ? "🥇 1" : (item.rank == 2 ? "🥈 2" : (item.rank == 3 ? "🥉 3" : "#" + item.rank));
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
            lblMyAvatar.setIcon(AvatarUtil.getAvatarIcon(avatarUrl, 54));
        }
    }
}
