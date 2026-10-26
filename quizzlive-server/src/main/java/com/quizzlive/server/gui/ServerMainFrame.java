package com.quizzlive.server.gui;

import com.quizzlive.server.dao.MatchConfigDAO;
import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.StatisticDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.*;
import com.quizzlive.server.network.SocketServer;
import com.quizzlive.shared.dto.LeaderboardDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ServerMainFrame extends JFrame {
    private final SocketServer server;
    private final UserDAO userDAO = new UserDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final MatchConfigDAO configDAO = new MatchConfigDAO();
    private final StatisticDAO statisticDAO = new StatisticDAO();

    // UI Components
    private JLabel lblOnlineUsers;
    private JLabel lblTotalUsers;
    private JTable tblUsers;
    private DefaultTableModel userTableModel;

    private JComboBox<String> cbFilterTopic;
    private JComboBox<String> cbFilterDifficulty;
    private JTable tblQuestions;
    private DefaultTableModel questionTableModel;

    // Config inputs
    private JSpinner spinQuestionCount;
    private JSpinner spinTimePerQuestion;
    private JSpinner spinBaseScore;
    private JCheckBox chkSpeedBonus;

    // Stats tables
    private JTable tblTopWinners;
    private JTable tblTopStreaks;
    private JTable tblMostPlayed;

    public ServerMainFrame(SocketServer server) {
        this.server = server;
        setTitle("Quizzlive Server - Bảng Quản Trị Hệ Thống (Nhóm 07 - Đề tài 09)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadAllData();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("📊 Trạng Thái & Người Dùng", createUsersPanel());
        tabbedPane.addTab("📚 Ngân Hàng Câu Hỏi", createQuestionsPanel());
        tabbedPane.addTab("⚙️ Cấu Hình Trận Đấu", createConfigPanel());
        tabbedPane.addTab("🏆 Thống Kê & Báo Cáo", createStatsPanel());

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    // TAB 1: User & Server Status (Requirement 15)
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top info bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topBar.setBackground(new Color(245, 247, 250));
        lblOnlineUsers = new JLabel("Đang online: 0");
        lblOnlineUsers.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOnlineUsers.setForeground(new Color(39, 174, 96));

        lblTotalUsers = new JLabel("Tổng người dùng: 0");
        lblTotalUsers.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnRefreshUsers = new JButton("Làm mới danh sách");
        btnRefreshUsers.addActionListener(e -> loadUsersData());

        JButton btnBlock = new JButton("Khóa / Mở khóa người dùng");
        btnBlock.setForeground(Color.RED);
        btnBlock.addActionListener(e -> toggleBlockUser());

        topBar.add(lblOnlineUsers);
        topBar.add(lblTotalUsers);
        topBar.add(btnRefreshUsers);
        topBar.add(btnBlock);

        panel.add(topBar, BorderLayout.NORTH);

        // User table
        String[] columns = {"Mã ID", "Tên đăng nhập", "Email", "Trạng thái", "Online", "Ngày tạo"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblUsers = new JTable(userTableModel);
        panel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);

        return panel;
    }

    // TAB 2: Question Bank (Requirement 12)
    private JPanel createQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        cbFilterTopic = new JComboBox<>(new String[]{"Tất cả chủ đề", "1. Khoa học Tự nhiên", "2. Lịch sử & Địa lý", "3. Công nghệ & Máy tính", "4. Tư duy IQ & Logic"});
        cbFilterDifficulty = new JComboBox<>(new String[]{"Tất cả độ khó", "EASY", "MEDIUM", "HARD"});
        JButton btnFilter = new JButton("Lọc danh sách");
        btnFilter.addActionListener(e -> loadQuestionsData());

        JButton btnAddQuestion = new JButton("+ Thêm câu hỏi mới");
        btnAddQuestion.setBackground(new Color(41, 128, 185));
        btnAddQuestion.setForeground(Color.WHITE);
        btnAddQuestion.addActionListener(e -> showAddQuestionDialog());

        JButton btnDeleteQuestion = new JButton("Xóa câu hỏi");
        btnDeleteQuestion.setForeground(Color.RED);
        btnDeleteQuestion.addActionListener(e -> deleteSelectedQuestion());

        filterPanel.add(new JLabel("Chủ đề:"));
        filterPanel.add(cbFilterTopic);
        filterPanel.add(new JLabel("Độ khó:"));
        filterPanel.add(cbFilterDifficulty);
        filterPanel.add(btnFilter);
        filterPanel.add(btnAddQuestion);
        filterPanel.add(btnDeleteQuestion);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Questions table
        String[] qCols = {"ID", "Chủ đề", "Loại", "Độ khó", "Nội dung câu hỏi", "Đáp án đúng", "Hình ảnh"};
        questionTableModel = new DefaultTableModel(qCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblQuestions = new JTable(questionTableModel);
        panel.add(new JScrollPane(tblQuestions), BorderLayout.CENTER);

        return panel;
    }

    // TAB 3: Match Configuration (Requirement 13)
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        spinQuestionCount = new JSpinner(new SpinnerNumberModel(5, 3, 20, 1));
        spinTimePerQuestion = new JSpinner(new SpinnerNumberModel(10, 5, 60, 1));
        spinBaseScore = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10));
        chkSpeedBonus = new JCheckBox("Bật thưởng điểm theo thời gian trả lời nhanh (Speed Bonus)", true);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Số câu hỏi mỗi trận đấu:"), gbc);
        gbc.gridx = 1;
        panel.add(spinQuestionCount, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Thời gian tối đa mỗi câu (giây):"), gbc);
        gbc.gridx = 1;
        panel.add(spinTimePerQuestion, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Điểm cơ bản mỗi câu đúng:"), gbc);
        gbc.gridx = 1;
        panel.add(spinBaseScore, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(chkSpeedBonus, gbc);

        JButton btnSaveConfig = new JButton("Lưu cấu hình trận đấu");
        btnSaveConfig.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSaveConfig.setBackground(new Color(39, 174, 96));
        btnSaveConfig.setForeground(Color.WHITE);
        btnSaveConfig.addActionListener(e -> saveMatchConfig());

        gbc.gridy = 4;
        panel.add(btnSaveConfig, gbc);

        return panel;
    }

    // TAB 4: Statistics & Reports (Requirement 16)
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sub-panel 1: Top Winners
        JPanel pnl1 = new JPanel(new BorderLayout());
        pnl1.setBorder(BorderFactory.createTitledBorder("Top Người Dùng Thắng Nhiều Nhất"));
        tblTopWinners = new JTable(new DefaultTableModel(new String[]{"Hạng", "User", "Họ tên", "Số trận thắng", "Tổng trận", "Điểm IQ"}, 0));
        pnl1.add(new JScrollPane(tblTopWinners), BorderLayout.CENTER);

        // Sub-panel 2: Top Streaks
        JPanel pnl2 = new JPanel(new BorderLayout());
        pnl2.setBorder(BorderFactory.createTitledBorder("Top Chuỗi Thắng Dài Nhất"));
        tblTopStreaks = new JTable(new DefaultTableModel(new String[]{"Hạng", "User", "Họ tên", "Chuỗi thắng dài nhất", "Chuỗi hiện tại"}, 0));
        pnl2.add(new JScrollPane(tblTopStreaks), BorderLayout.CENTER);

        // Sub-panel 3: Most Played
        JPanel pnl3 = new JPanel(new BorderLayout());
        pnl3.setBorder(BorderFactory.createTitledBorder("Top Người Dùng Tham Gia Nhiều Trận Nhất"));
        tblMostPlayed = new JTable(new DefaultTableModel(new String[]{"Hạng", "User", "Họ tên", "Tổng số trận", "Thắng", "Tỉ lệ thắng (%)"}, 0));
        pnl3.add(new JScrollPane(tblMostPlayed), BorderLayout.CENTER);

        panel.add(pnl1);
        panel.add(pnl2);
        panel.add(pnl3);

        return panel;
    }

    private void loadAllData() {
        loadUsersData();
        loadQuestionsData();
        loadConfigData();
        loadStatsData();
    }

    private void loadUsersData() {
        lblOnlineUsers.setText("Đang online: " + statisticDAO.getOnlineUserCount());
        lblTotalUsers.setText("Tổng người dùng: " + statisticDAO.getTotalUserCount());

        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getStatus(),
                    u.isOnline() ? "Online" : "Offline",
                    u.getCreatedAt()
            });
        }
    }

    private void toggleBlockUser() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 người dùng trên bảng!");
            return;
        }

        Long userId = (Long) userTableModel.getValueAt(row, 0);
        String currentStatus = (String) userTableModel.getValueAt(row, 3);
        boolean isBlocked = "BLOCKED".equalsIgnoreCase(currentStatus);

        userDAO.blockUser(userId, !isBlocked);
        JOptionPane.showMessageDialog(this, isBlocked ? "Đã mở khóa người dùng!" : "Đã khóa người dùng thành công!");
        loadUsersData();
    }

    private void loadQuestionsData() {
        questionTableModel.setRowCount(0);
        int topicIdx = cbFilterTopic.getSelectedIndex();
        Integer topicId = topicIdx > 0 ? topicIdx : null;

        String diff = (String) cbFilterDifficulty.getSelectedItem();
        if ("Tất cả độ khó".equals(diff)) diff = "ALL";

        List<Question> questions = questionDAO.getAllQuestionsForAdmin(topicId, diff);
        for (Question q : questions) {
            String correctAns = "N/A";
            for (Answer a : q.getAnswers()) {
                if (a.isCorrect()) {
                    correctAns = a.getKeySymbol() + ": " + a.getContent();
                    break;
                }
            }
            questionTableModel.addRow(new Object[]{
                    q.getQuestionId(),
                    q.getTopicName(),
                    q.getQuestionType(),
                    q.getDifficulty(),
                    q.getContent(),
                    correctAns,
                    q.getImage() != null ? q.getImage() : "Không có"
            });
        }
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog(this, "Thêm Câu Hỏi Mới", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(9, 2, 8, 8));

        JComboBox<String> cbTopic = new JComboBox<>(new String[]{"1. Khoa học Tự nhiên", "2. Lịch sử & Địa lý", "3. Công nghệ & Máy tính", "4. Tư duy IQ & Logic"});
        JComboBox<String> cbDiff = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD"});
        JTextField txtContent = new JTextField();
        JTextField txtAnsA = new JTextField();
        JTextField txtAnsB = new JTextField();
        JTextField txtAnsC = new JTextField();
        JTextField txtAnsD = new JTextField();
        JComboBox<String> cbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        dialog.add(new JLabel("Chủ đề:")); dialog.add(cbTopic);
        dialog.add(new JLabel("Độ khó:")); dialog.add(cbDiff);
        dialog.add(new JLabel("Nội dung câu hỏi:")); dialog.add(txtContent);
        dialog.add(new JLabel("Đáp án A:")); dialog.add(txtAnsA);
        dialog.add(new JLabel("Đáp án B:")); dialog.add(txtAnsB);
        dialog.add(new JLabel("Đáp án C:")); dialog.add(txtAnsC);
        dialog.add(new JLabel("Đáp án D:")); dialog.add(txtAnsD);
        dialog.add(new JLabel("Đáp án đúng:")); dialog.add(cbCorrect);

        JButton btnSave = new JButton("Lưu Câu Hỏi");
        btnSave.addActionListener(e -> {
            if (txtContent.getText().trim().isEmpty() || txtAnsA.getText().trim().isEmpty() || txtAnsB.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ nội dung câu hỏi và các đáp án!");
                return;
            }

            Question q = new Question();
            q.setTopicId(cbTopic.getSelectedIndex() + 1);
            q.setDifficulty((String) cbDiff.getSelectedItem());
            q.setContent(txtContent.getText().trim());
            q.setQuestionType(cbTopic.getSelectedIndex() == 3 ? "IQ_IMAGE" : "STANDARD");

            List<Answer> ansList = new ArrayList<>();
            String correctSymbol = (String) cbCorrect.getSelectedItem();
            ansList.add(new Answer(null, null, "A", txtAnsA.getText().trim(), "A".equals(correctSymbol)));
            ansList.add(new Answer(null, null, "B", txtAnsB.getText().trim(), "B".equals(correctSymbol)));
            ansList.add(new Answer(null, null, "C", txtAnsC.getText().trim(), "C".equals(correctSymbol)));
            ansList.add(new Answer(null, null, "D", txtAnsD.getText().trim(), "D".equals(correctSymbol)));
            q.setAnswers(ansList);

            questionDAO.saveQuestionWithAnswers(q);
            JOptionPane.showMessageDialog(dialog, "Thêm câu hỏi mới thành công!");
            dialog.dispose();
            loadQuestionsData();
        });

        dialog.add(new JLabel("")); dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private void deleteSelectedQuestion() {
        int row = tblQuestions.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 câu hỏi trên bảng để xóa!");
            return;
        }

        Long qId = (Long) questionTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa câu hỏi ID: " + qId + " không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            questionDAO.deleteQuestion(qId);
            loadQuestionsData();
        }
    }

    private void loadConfigData() {
        MatchConfig cfg = configDAO.getActiveConfig();
        spinQuestionCount.setValue(cfg.getQuestionCount());
        spinTimePerQuestion.setValue(cfg.getTimePerQuestion());
        spinBaseScore.setValue(cfg.getBaseScore());
        chkSpeedBonus.setSelected(cfg.isSpeedBonus());
    }

    private void saveMatchConfig() {
        MatchConfig cfg = configDAO.getActiveConfig();
        cfg.setQuestionCount((Integer) spinQuestionCount.getValue());
        cfg.setTimePerQuestion((Integer) spinTimePerQuestion.getValue());
        cfg.setBaseScore((Integer) spinBaseScore.getValue());
        cfg.setSpeedBonus(chkSpeedBonus.isSelected());

        configDAO.updateConfig(cfg);
        JOptionPane.showMessageDialog(this, "Đã lưu và áp dụng cấu hình trận đấu thành công!");
    }

    private void loadStatsData() {
        // Top winners
        DefaultTableModel m1 = (DefaultTableModel) tblTopWinners.getModel();
        m1.setRowCount(0);
        for (LeaderboardDTO.LeaderboardItem it : statisticDAO.getTopWinners(5)) {
            m1.addRow(new Object[]{it.rank, it.username, it.fullName, it.winMatches, it.totalMatches, it.iqScore});
        }

        // Top streaks
        DefaultTableModel m2 = (DefaultTableModel) tblTopStreaks.getModel();
        m2.setRowCount(0);
        for (LeaderboardDTO.LeaderboardItem it : statisticDAO.getTopStreaks(5)) {
            m2.addRow(new Object[]{it.rank, it.username, it.fullName, it.maxStreak, it.currentStreak});
        }

        // Most played
        DefaultTableModel m3 = (DefaultTableModel) tblMostPlayed.getModel();
        m3.setRowCount(0);
        for (LeaderboardDTO.LeaderboardItem it : statisticDAO.getMostPlayed(5)) {
            m3.addRow(new Object[]{it.rank, it.username, it.fullName, it.totalMatches, it.winMatches, String.format("%.1f%%", it.winRate)});
        }
    }
}
