package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientTournamentService;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.shared.dto.TournamentDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Giải Đấu (Tournament) - Chuẩn phong cách NEO-BRUTALIST (Nền trắng viền đen)
 * Theo thiết kế SCREEN 8 & SCREEN 9 trong master_gui_showcase.html
 * 
 * Phụ trách Yêu cầu 9: "Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có bảng xếp hạng của giải"
 */
public class TournamentPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientTournamentService tournamentService = new ClientTournamentService();

    private DefaultTableModel modelTournaments;
    private JTable tblTournaments;

    private DefaultTableModel modelStandings;
    private JTable tblStandings;

    private BrutalUI.BrutalButton btnJoinTournament;
    private BrutalUI.BrutalButton btnViewDetails;
    private JLabel lblTournamentCountBadge;

    public TournamentPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BrutalUI.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // =========================================================================
        // 1. TOP HEADER: Card viền đen, Title, Badge & Navigation Buttons
        // =========================================================================
        BrutalUI.CardPanel headerCard = new BrutalUI.CardPanel(16, 3);
        headerCard.setLayout(new BorderLayout(15, 10));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        headerLeft.setOpaque(false);

        JPanel tagLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tagLine.setOpaque(false);
        JLabel badgeTag = BrutalUI.createBadge("🎖️ GIẢI ĐẤU TRANH TÀI THEO BẢNG / VÒNG (YÊU CẦU 9)", new Color(254, 243, 199), new Color(146, 64, 14));
        tagLine.add(badgeTag);

        JLabel lblTitle = new JLabel("ĐẤU TRƯỜNG GIẢI ĐẤU TOÀN TRƯỜNG");
        lblTitle.setFont(BrutalUI.FONT_TITLE_XL);
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);

        headerLeft.add(tagLine);
        headerLeft.add(lblTitle);
        headerCard.add(headerLeft, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        headerActions.setOpaque(false);

        BrutalUI.BrutalButton btnRefresh = new BrutalUI.BrutalButton("🔄 Làm Mới", BrutalUI.BrutalButton.Style.OUTLINE, 10);
        btnRefresh.setPreferredSize(new Dimension(115, 38));
        btnRefresh.addActionListener(e -> tournamentService.requestTournamentList());

        BrutalUI.BrutalButton btnBack = new BrutalUI.BrutalButton("⬅️ Về Phòng Chờ", BrutalUI.BrutalButton.Style.PRIMARY, 10);
        btnBack.setPreferredSize(new Dimension(145, 38));
        btnBack.addActionListener(e -> mainFrame.showPanel(MainClientFrame.CARD_LOBBY));

        headerActions.add(btnRefresh);
        headerActions.add(btnBack);
        headerCard.add(headerActions, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // =========================================================================
        // 2. CENTER CONTENT: 2 Cột Card (Danh Sách Giải Đấu & Bảng Xếp Hạng Giải)
        // =========================================================================
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // --- CỘT TRÁI: DANH SÁCH GIẢI ĐẤU (SCREEN 8) ---
        BrutalUI.CardPanel leftCard = new BrutalUI.CardPanel(16, 4);
        leftCard.setLayout(new BorderLayout(10, 10));

        JPanel leftHeader = new JPanel(new BorderLayout(8, 0));
        leftHeader.setOpaque(false);
        leftHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 8, 4));

        JLabel lblLeftTitle = new JLabel("🏆 Danh Sách Giải Đấu Đang Mở");
        lblLeftTitle.setFont(BrutalUI.FONT_BODY_BOLD);
        lblLeftTitle.setForeground(BrutalUI.COLOR_BLACK);

        lblTournamentCountBadge = BrutalUI.createBadge("0 giải", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);
        leftHeader.add(lblLeftTitle, BorderLayout.WEST);
        leftHeader.add(lblTournamentCountBadge, BorderLayout.EAST);
        leftCard.add(leftHeader, BorderLayout.NORTH);

        modelTournaments = new DefaultTableModel(new String[]{"ID", "Tên giải đấu", "Trạng thái", "Thí sinh", "Bắt đầu lúc"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTournaments = new JTable(modelTournaments);
        styleBrutalTable(tblTournaments);

        // Tự động xem bảng đấu khi click chọn giải
        tblTournaments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblTournaments.getSelectedRow() >= 0) {
                handleViewStandings();
            }
        });

        JScrollPane scrollTournaments = new JScrollPane(tblTournaments);
        scrollTournaments.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));
        scrollTournaments.getViewport().setBackground(Color.WHITE);
        leftCard.add(scrollTournaments, BorderLayout.CENTER);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        leftActions.setOpaque(false);

        btnViewDetails = new BrutalUI.BrutalButton("🔍 Xem Bảng Đấu", BrutalUI.BrutalButton.Style.OUTLINE, 12);
        btnViewDetails.setPreferredSize(new Dimension(140, 40));
        btnViewDetails.addActionListener(e -> handleViewStandings());

        btnJoinTournament = new BrutalUI.BrutalButton("✨ Đăng Ký Tham Gia", BrutalUI.BrutalButton.Style.SUCCESS, 12);
        btnJoinTournament.setPreferredSize(new Dimension(170, 40));
        btnJoinTournament.addActionListener(e -> handleJoinTournament());

        leftActions.add(btnViewDetails);
        leftActions.add(btnJoinTournament);
        leftCard.add(leftActions, BorderLayout.SOUTH);

        centerGrid.add(leftCard);

        // --- CỘT PHẢI: BẢNG XẾP HẠNG & VÒNG BẢNG (SCREEN 9) ---
        BrutalUI.CardPanel rightCard = new BrutalUI.CardPanel(16, 4);
        rightCard.setLayout(new BorderLayout(10, 10));

        JPanel rightHeader = new JPanel(new BorderLayout(8, 0));
        rightHeader.setOpaque(false);
        rightHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 8, 4));

        JLabel lblRightTitle = new JLabel("📊 Bảng Xếp Hạng Vòng / Bảng Đấu");
        lblRightTitle.setFont(BrutalUI.FONT_BODY_BOLD);
        lblRightTitle.setForeground(BrutalUI.COLOR_BLACK);

        JLabel ruleBadge = BrutalUI.createBadge("Thắng +3đ · Hòa +1đ · Thua 0đ", new Color(239, 246, 255), new Color(29, 78, 216));
        rightHeader.add(lblRightTitle, BorderLayout.WEST);
        rightHeader.add(ruleBadge, BorderLayout.EAST);
        rightCard.add(rightHeader, BorderLayout.NORTH);

        modelStandings = new DefaultTableModel(new String[]{"Thứ hạng", "Thí sinh", "Bảng", "Số trận", "Thắng", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStandings = new JTable(modelStandings);
        styleBrutalTable(tblStandings);

        // Renderer đặc biệt cho thứ hạng (Huy hiệu 🥇 🥈 🥉)
        tblStandings.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(BrutalUI.FONT_BODY_BOLD);
                String valStr = value != null ? value.toString() : "";
                if ("1".equals(valStr)) {
                    lbl.setText("🥇 1");
                    lbl.setForeground(new Color(180, 83, 9));
                } else if ("2".equals(valStr)) {
                    lbl.setText("🥈 2");
                    lbl.setForeground(new Color(75, 85, 99));
                } else if ("3".equals(valStr)) {
                    lbl.setText("🥉 3");
                    lbl.setForeground(new Color(180, 83, 9));
                } else {
                    lbl.setText("  " + valStr);
                    lbl.setForeground(BrutalUI.COLOR_BLACK);
                }
                return lbl;
            }
        });

        JScrollPane scrollStandings = new JScrollPane(tblStandings);
        scrollStandings.setBorder(BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2));
        scrollStandings.getViewport().setBackground(Color.WHITE);
        rightCard.add(scrollStandings, BorderLayout.CENTER);

        // Chú thích dưới cùng
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        rightFooter.setOpaque(false);
        JLabel lblNotice = new JLabel("🎯 Bảng đấu tự động cập nhật ngay khi mỗi trận đấu giải kết thúc.");
        lblNotice.setFont(BrutalUI.FONT_SMALL);
        lblNotice.setForeground(BrutalUI.COLOR_MUTED);
        rightFooter.add(lblNotice);
        rightCard.add(rightFooter, BorderLayout.SOUTH);

        centerGrid.add(rightCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    /**
     * Tùy biến bảng JTable chuẩn phong cách Neo-Brutalist (Viền đen, nền trắng, hàng cao, font rõ)
     */
    private void styleBrutalTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(BrutalUI.FONT_BODY);
        table.setSelectionBackground(new Color(254, 243, 199)); // Vàng pastel khi chọn
        table.setSelectionForeground(BrutalUI.COLOR_BLACK);
        table.setGridColor(new Color(228, 228, 231));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(BrutalUI.FONT_BODY_BOLD);
        header.setBackground(new Color(244, 244, 245));
        header.setForeground(BrutalUI.COLOR_BLACK);
        header.setPreferredSize(new Dimension(100, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BrutalUI.COLOR_BLACK));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 1) { // Ngoại trừ cột tên
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void handleJoinTournament() {
        int row = tblTournaments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 giải đấu để đăng ký!", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long tourId = Long.parseLong(modelTournaments.getValueAt(row, 0).toString());
        String title = modelTournaments.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng ký tham gia giải đấu:\n[" + title + "] không?",
                "Xác nhận đăng ký giải",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            tournamentService.joinTournament(tourId);
        }
    }

    private void handleViewStandings() {
        int row = tblTournaments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 giải đấu để xem bảng đấu!", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long tourId = Long.parseLong(modelTournaments.getValueAt(row, 0).toString());
        tournamentService.requestTournamentStandings(tourId);
    }

    public void renderTournamentList(List<TournamentDTO.TournamentItem> list) {
        modelTournaments.setRowCount(0);
        int count = 0;
        if (list != null) {
            count = list.size();
            for (TournamentDTO.TournamentItem item : list) {
                modelTournaments.addRow(new Object[]{
                        item.tournamentId,
                        item.title,
                        item.status,
                        item.currentParticipants + " / " + item.maxParticipants,
                        item.startTime
                });
            }
        }
        lblTournamentCountBadge.setText(count + " giải đấu");

        // Nếu có giải đấu, tự động chọn dòng đầu tiên để load BXH
        if (tblTournaments.getRowCount() > 0 && tblTournaments.getSelectedRow() < 0) {
            tblTournaments.setRowSelectionInterval(0, 0);
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
