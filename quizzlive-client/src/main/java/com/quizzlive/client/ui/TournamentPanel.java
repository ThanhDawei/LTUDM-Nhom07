package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientTournamentService;
import com.quizzlive.shared.dto.TournamentDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Giải Đấu (Tournament)
 * Phụ trách Yêu cầu 9: "Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có bảng xếp hạng của giải"
 */
public class TournamentPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientTournamentService tournamentService = new ClientTournamentService();

    private DefaultTableModel modelTournaments;
    private JTable tblTournaments;

    private DefaultTableModel modelStandings;
    private JTable tblStandings;

    private JButton btnJoinTournament;
    private JButton btnViewDetails;

    public TournamentPanel(MainClientFrame mainFrame) {
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

        JLabel lblTitle = new JLabel("🎖️ GIẢI ĐẤU TRỰC TUYẾN THEO VÒNG / BẢNG (Yêu cầu 9)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(142, 68, 173));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRefresh = new JButton("Làm Mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> tournamentService.requestTournamentList());

        JButton btnBack = new JButton("⬅️ Quay lại Phòng Chờ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOBBY));

        actions.add(btnRefresh);
        actions.add(btnBack);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. CENTER CONTENT (2 Panels: Tournament List & Bracket/Standings)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setOpaque(false);

        // Left Panel: Tournaments List
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblLeftTitle = new JLabel("Danh Sách Giải Đấu");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(lblLeftTitle, BorderLayout.NORTH);

        modelTournaments = new DefaultTableModel(new String[]{"ID", "Tên giải đấu", "Trạng thái", "Số người", "Bắt đầu lúc"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTournaments = new JTable(modelTournaments);
        tblTournaments.setRowHeight(24);
        leftPanel.add(new JScrollPane(tblTournaments), BorderLayout.CENTER);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        leftActions.setOpaque(false);

        btnJoinTournament = new JButton("Đăng Ký Tham Gia");
        btnJoinTournament.setBackground(new Color(39, 174, 96));
        btnJoinTournament.setForeground(Color.WHITE);
        btnJoinTournament.setFocusPainted(false);
        btnJoinTournament.addActionListener(e -> handleJoinTournament());

        btnViewDetails = new JButton("Xem Bảng Đấu");
        btnViewDetails.setFocusPainted(false);
        btnViewDetails.addActionListener(e -> handleViewStandings());

        leftActions.add(btnViewDetails);
        leftActions.add(btnJoinTournament);
        leftPanel.add(leftActions, BorderLayout.SOUTH);
        contentPanel.add(leftPanel);

        // Right Panel: Standings / Bracket
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblRightTitle = new JLabel("Bảng Xếp Hạng Vòng / Bảng Đấu");
        lblRightTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rightPanel.add(lblRightTitle, BorderLayout.NORTH);

        modelStandings = new DefaultTableModel(new String[]{"Thứ hạng", "Người chơi", "Bảng", "Số trận", "Số trận thắng", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStandings = new JTable(modelStandings);
        tblStandings.setRowHeight(24);
        rightPanel.add(new JScrollPane(tblStandings), BorderLayout.CENTER);
        contentPanel.add(rightPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void handleJoinTournament() {
        int row = tblTournaments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 giải đấu để đăng ký!");
            return;
        }

        long tourId = Long.parseLong(modelTournaments.getValueAt(row, 0).toString());
        String title = modelTournaments.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng ký tham gia giải [" + title + "] không?", "Đăng ký giải đấu", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tournamentService.joinTournament(tourId);
        }
    }

    private void handleViewStandings() {
        int row = tblTournaments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 giải đấu để xem bảng đấu!");
            return;
        }

        long tourId = Long.parseLong(modelTournaments.getValueAt(row, 0).toString());
        tournamentService.requestTournamentStandings(tourId);
    }

    public void renderTournamentList(List<TournamentDTO.TournamentItem> list) {
        modelTournaments.setRowCount(0);
        if (list != null) {
            for (TournamentDTO.TournamentItem item : list) {
                modelTournaments.addRow(new Object[]{
                        item.tournamentId,
                        item.title,
                        item.status,
                        item.currentParticipants + "/" + item.maxParticipants,
                        item.startTime
                });
            }
        }
    }

    public void renderStandings(List<TournamentDTO.StandingItem> standings) {
        modelStandings.setRowCount(0);
        if (standings != null) {
            for (TournamentDTO.StandingItem item : standings) {
                modelStandings.addRow(new Object[]{
                        item.rank,
                        item.username,
                        item.groupName,
                        item.matchesPlayed,
                        item.wins,
                        item.points
                });
            }
        }
    }
}
