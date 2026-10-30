package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientLeaderboardService;
import com.quizzlive.shared.dto.LeaderboardDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Bảng Xếp Hạng & Lịch Sử Đấu
 * Phụ trách Yêu cầu 10: "Xếp hạng chung: thứ hạng, chuỗi thắng/thua dài nhất, tỉ lệ thắng/thua; xem lịch sử trận"
 */
public class LeaderboardPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientLeaderboardService leaderboardService = new ClientLeaderboardService();

    private JTabbedPane tabbedPane;

    // Tab 1: Top Thắng
    private DefaultTableModel modelTopWins;
    private JTable tblTopWins;

    // Tab 2: Top Chuỗi Thắng
    private DefaultTableModel modelTopStreaks;
    private JTable tblTopStreaks;

    // Tab 3: Lịch Sử Đấu Cá Nhân
    private DefaultTableModel modelHistory;
    private JTable tblHistory;

    public LeaderboardPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
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
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(41, 128, 185));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRefresh = new JButton("Làm Mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> leaderboardService.requestLeaderboard());

        JButton btnBack = new JButton("⬅️ Quay lại Phòng Chờ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOBBY));

        actions.add(btnRefresh);
        actions.add(btnBack);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. TABS
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab 1: Top Thắng Nhiều Nhất
        modelTopWins = new DefaultTableModel(new String[]{"Thứ hạng", "Người chơi", "Tổng trận", "Số trận thắng", "Tỉ lệ thắng (%)", "Tổng điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTopWins = new JTable(modelTopWins);
        tblTopWins.setRowHeight(26);
        tabbedPane.addTab("🥇 Top Thắng Nhiều Nhất", new JScrollPane(tblTopWins));

        // Tab 2: Top Chuỗi Thắng
        modelTopStreaks = new DefaultTableModel(new String[]{"Thứ hạng", "Người chơi", "Chuỗi hiện tại", "Chuỗi thắng dài nhất", "Tổng điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTopStreaks = new JTable(modelTopStreaks);
        tblTopStreaks.setRowHeight(26);
        tabbedPane.addTab("🔥 Top Chuỗi Thắng Dài Nhất", new JScrollPane(tblTopStreaks));

        // Tab 3: Lịch Sử Đấu Cá Nhân
        modelHistory = new DefaultTableModel(new String[]{"Mã trận", "Đối thủ", "Kết quả", "Điểm của bạn", "Điểm đối thủ", "Thời gian"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(26);
        tabbedPane.addTab("📜 Lịch Sử Đấu Cá Nhân", new JScrollPane(tblHistory));

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void renderLeaderboard(LeaderboardDTO.LeaderboardResponse resp) {
        modelTopWins.setRowCount(0);
        modelTopStreaks.setRowCount(0);

        if (resp == null) return;

        if (resp.topPlayers != null) {
            for (LeaderboardDTO.LeaderboardItem item : resp.topPlayers) {
                modelTopWins.addRow(new Object[]{
                        item.rank,
                        item.username,
                        item.totalMatches,
                        item.winMatches,
                        String.format("%.1f%%", item.winRate),
                        item.totalPoints
                });
            }
        }

        if (resp.topStreaks != null) {
            for (LeaderboardDTO.LeaderboardItem item : resp.topStreaks) {
                modelTopStreaks.addRow(new Object[]{
                        item.rank,
                        item.username,
                        item.currentStreak,
                        item.maxStreak,
                        item.totalPoints
                });
            }
        }
    }

    public void renderMatchHistory(List<LeaderboardDTO.MatchHistoryItem> history) {
        modelHistory.setRowCount(0);
        if (history != null) {
            for (LeaderboardDTO.MatchHistoryItem h : history) {
                modelHistory.addRow(new Object[]{
                        h.matchCode,
                        h.opponentUsername,
                        h.result,
                        h.myScore,
                        h.opponentScore,
                        h.playedAt
                });
            }
        }
    }
}
