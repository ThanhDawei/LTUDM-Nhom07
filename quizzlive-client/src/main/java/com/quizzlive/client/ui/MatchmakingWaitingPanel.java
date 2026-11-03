package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Giao diện Hàng đợi Ghép cặp tự động và Làm bài Test IQ bằng hình ảnh
 * Phụ trách:
 * - Yêu cầu 2: "Ghép cặp tự động trong 60 giây; hết thời gian chưa ghép được
 * thì đưa về phòng chờ"
 * - Yêu cầu 11: "Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để
 * xếp hạng; điểm IQ lưu vào hồ sơ"
 * Cấu hình: Không dùng băm và mã hóa.
 */
public class MatchmakingWaitingPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientMatchmakingService matchmakingService = new ClientMatchmakingService();
    private final ClientIQService iqService = new ClientIQService();

    // Components cho Yêu cầu 2 (Đếm ngược 60s)
    private JLabel lblCountdown;
    private JProgressBar progressBar;
    private JButton btnCancelQueue;
    private Timer queueTimer;
    private int remainingSeconds = 60;
    private boolean isTimeoutHandled = false; // biến cờ chặn gọi trùng

    // Components cho Yêu cầu 11 (Test IQ bằng hình ảnh)
    private JLabel lblIqTitle;
    private JPanel pnlIqImageContainer;
    private JLabel lblIqImage;
    private JRadioButton rbOptA, rbOptB, rbOptC, rbOptD;
    private ButtonGroup bgIqOptions;
    private JButton btnSubmitIQ;
    private JButton btnNextIQ;
    private JLabel lblIqResult;
    private Long currentIqQuestionId;

    public MatchmakingWaitingPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 1. TOP PANEL: Yêu cầu 2 (Ghép cặp 60 giây)
        JPanel topQueuePanel = new JPanel(new BorderLayout(10, 10));
        topQueuePanel.setBackground(Color.WHITE);
        topQueuePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JPanel queueInfo = new JPanel(new GridLayout(2, 1, 5, 5));
        queueInfo.setOpaque(false);
        JLabel lblQueueHeader = new JLabel("ĐANG TÌM KIẾM ĐỐI THỦ THÍCH HỢP...", SwingConstants.CENTER);
        lblQueueHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQueueHeader.setForeground(new Color(41, 128, 185));

        lblCountdown = new JLabel("Thời gian chờ: 60s", SwingConstants.CENTER);
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCountdown.setForeground(new Color(231, 76, 60));

        queueInfo.add(lblQueueHeader);
        queueInfo.add(lblCountdown);
        topQueuePanel.add(queueInfo, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 60);
        progressBar.setValue(60);
        progressBar.setPreferredSize(new Dimension(300, 10));
        progressBar.setForeground(new Color(41, 128, 185));
        topQueuePanel.add(progressBar, BorderLayout.SOUTH);

        btnCancelQueue = new JButton("Hủy Tìm Kiếm");
        btnCancelQueue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelQueue.setBackground(new Color(189, 195, 199));
        btnCancelQueue.setFocusPainted(false);
        btnCancelQueue.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelQueue.addActionListener(e -> cancelQueueAndReturn());
        topQueuePanel.add(btnCancelQueue, BorderLayout.EAST);

        add(topQueuePanel, BorderLayout.NORTH);

        // 2. CENTER PANEL: Yêu cầu 11 (Test IQ bằng hình ảnh)
        JPanel iqCard = new JPanel(new BorderLayout(15, 15));
        iqCard.setBackground(Color.WHITE);
        iqCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        JPanel iqHeader = new JPanel(new GridLayout(2, 1));
        iqHeader.setOpaque(false);
        lblIqTitle = new JLabel("🧠 THỬ TÀI IQ HÌNH ẢNH TRONG LÚC CHỜ GHÉP CẶP (Yêu cầu 11)");
        lblIqTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIqTitle.setForeground(new Color(142, 68, 173));

        JLabel lblIqNote = new JLabel("Điểm IQ được lưu vào hồ sơ cá nhân và không ảnh hưởng đến điểm thi đấu.");
        lblIqNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblIqNote.setForeground(Color.GRAY);

        iqHeader.add(lblIqTitle);
        iqHeader.add(lblIqNote);
        iqCard.add(iqHeader, BorderLayout.NORTH);

        // Khung hiển thị ảnh IQ (Tự vẽ đồ họa quy luật hình học nếu không có ảnh tĩnh)
        pnlIqImageContainer = new JPanel(new BorderLayout());
        pnlIqImageContainer.setBackground(new Color(250, 250, 252));
        pnlIqImageContainer.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 230), 1));
        pnlIqImageContainer.setPreferredSize(new Dimension(380, 160));

        lblIqImage = new JLabel("[ Đang nạp câu đố IQ hình ảnh... ]", SwingConstants.CENTER);
        lblIqImage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIqImage.setForeground(new Color(52, 73, 94));
        pnlIqImageContainer.add(lblIqImage, BorderLayout.CENTER);
        iqCard.add(pnlIqImageContainer, BorderLayout.CENTER);

        // Các lựa chọn đáp án IQ
        JPanel iqBottom = new JPanel(new BorderLayout(10, 10));
        iqBottom.setOpaque(false);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        optionsPanel.setOpaque(false);
        rbOptA = createOptionRadio("A. Đáp án A");
        rbOptB = createOptionRadio("B. Đáp án B");
        rbOptC = createOptionRadio("C. Đáp án C");
        rbOptD = createOptionRadio("D. Đáp án D");

        bgIqOptions = new ButtonGroup();
        bgIqOptions.add(rbOptA);
        bgIqOptions.add(rbOptB);
        bgIqOptions.add(rbOptC);
        bgIqOptions.add(rbOptD);

        optionsPanel.add(rbOptA);
        optionsPanel.add(rbOptB);
        optionsPanel.add(rbOptC);
        optionsPanel.add(rbOptD);
        iqBottom.add(optionsPanel, BorderLayout.CENTER);

        JPanel iqActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        iqActions.setOpaque(false);

        lblIqResult = new JLabel(" ");
        lblIqResult.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnNextIQ = new JButton("Đổi câu khác");
        btnNextIQ.setFocusPainted(false);
        btnNextIQ.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNextIQ.addActionListener(e -> {
            lblIqResult.setText("");
            iqService.requestIQQuestion();
        });

        btnSubmitIQ = new JButton("Nộp Đáp Án IQ");
        btnSubmitIQ.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmitIQ.setBackground(new Color(142, 68, 173));
        btnSubmitIQ.setForeground(Color.WHITE);
        btnSubmitIQ.setFocusPainted(false);
        btnSubmitIQ.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmitIQ.addActionListener(e -> handleSubmitIQ());

        iqActions.add(lblIqResult);
        iqActions.add(btnNextIQ);
        iqActions.add(btnSubmitIQ);
        iqBottom.add(iqActions, BorderLayout.SOUTH);

        iqCard.add(iqBottom, BorderLayout.SOUTH);
        add(iqCard, BorderLayout.CENTER);
    }

    private JRadioButton createOptionRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setOpaque(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    /**
     * Bắt đầu đếm ngược 60 giây khi người dùng bấm Tìm trận
     */
    public void startCountdown(int seconds) {
        this.isTimeoutHandled = false; // Reset lại cờ khi người chơi bấm tìm trận mới
        this.remainingSeconds = seconds;
        progressBar.setMaximum(seconds);
        progressBar.setValue(seconds);
        lblCountdown.setText("Thời gian chờ: " + remainingSeconds + "s");

        if (queueTimer != null && queueTimer.isRunning()) {
            queueTimer.stop();
        }

        queueTimer = new Timer(1000, e -> {
            remainingSeconds--;
            lblCountdown.setText("Thời gian chờ: " + remainingSeconds + "s");
            progressBar.setValue(remainingSeconds);

            if (remainingSeconds <= 0) {
                queueTimer.stop();
                handleQueueTimeout();
            }
        });
        queueTimer.start();

        // Gửi yêu cầu lấy 1 câu đố IQ hình ảnh để người chơi giải trí trong lúc chờ
        lblIqResult.setText("");
        iqService.requestIQQuestion();
    }

    public void stopCountdown() {
        if (queueTimer != null) {
            queueTimer.stop();
        }
    }

    private void cancelQueueAndReturn() {
        this.isTimeoutHandled = true; // Chặn luôn để không bị hiện thông báo sau khi đã bấm hủy
        stopCountdown();
        matchmakingService.leaveQueue();
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    /**
     * Xử lý khi hết 60 giây chưa tìm thấy đối thủ (Yêu cầu 2)
     */
    public void handleQueueTimeout() {
        if (isTimeoutHandled)
            return; // Nếu đã hiện thông báo rồi thì BỎ QUA, không hiện lần 2
        isTimeoutHandled = true;
        stopCountdown();
        JOptionPane.showMessageDialog(this,
                "Hết thời gian 60 giây chưa tìm thấy đối thủ phù hợp!\nĐưa người chơi về phòng chờ.",
                "Thông báo ghép cặp (YC 2)", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    private void handleSubmitIQ() {
        if (currentIqQuestionId == null) {
            JOptionPane.showMessageDialog(this, "Chưa có câu hỏi IQ nào!");
            return;
        }

        Long selectedAnswerId = null;
        if (rbOptA.isSelected())
            selectedAnswerId = (Long) rbOptA.getClientProperty("answerId");
        else if (rbOptB.isSelected())
            selectedAnswerId = (Long) rbOptB.getClientProperty("answerId");
        else if (rbOptC.isSelected())
            selectedAnswerId = (Long) rbOptC.getClientProperty("answerId");
        else if (rbOptD.isSelected())
            selectedAnswerId = (Long) rbOptD.getClientProperty("answerId");

        if (selectedAnswerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 đáp án trước khi nộp!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSubmitIQ.setEnabled(false);
        lblIqResult.setForeground(Color.GRAY);
        lblIqResult.setText("Đang chấm điểm...");

        iqService.submitIQAnswer(currentIqQuestionId, selectedAnswerId);
    }

    /**
     * Hiển thị câu hỏi IQ hình ảnh nhận được từ Server (Yêu cầu 11)
     */
    public void renderIQQuestion(IQTestDTO.IQQuestionNotify question) {
        if (question == null)
            return;
        this.currentIqQuestionId = question.questionId;
        lblIqTitle.setText(
                "🧠 " + (question.content != null ? question.content : "Quan sát hình ảnh và chọn đáp án đúng:"));
        lblIqResult.setText("");
        btnSubmitIQ.setEnabled(true);
        bgIqOptions.clearSelection();

        // Gán đáp án vào các radio button
        List<BattleDTO.QuestionAnswerOption> options = question.options;
        JRadioButton[] rbs = new JRadioButton[] { rbOptA, rbOptB, rbOptC, rbOptD };
        for (int i = 0; i < rbs.length; i++) {
            if (options != null && i < options.size()) {
                BattleDTO.QuestionAnswerOption opt = options.get(i);
                rbs[i].setText(opt.key + ". " + opt.content);
                rbs[i].putClientProperty("answerId", opt.answerId);
                rbs[i].setVisible(true);
                rbs[i].setEnabled(true);
            } else {
                rbs[i].setVisible(false);
            }
        }

        // Cập nhật khung hình ảnh mô phỏng trực quan
        renderImagePuzzle(question.imageUrl, question.content);
    }

    /**
     * Hiển thị hình ảnh hoặc bản vẽ đồ họa mô phỏng quy luật logic của câu hỏi IQ
     */
    private void renderImagePuzzle(String imageUrl, String content) {
        // Kiểm tra xem có file ảnh thực tế trên ổ đĩa không
        if (imageUrl != null && new File(imageUrl).exists()) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image img = icon.getImage().getScaledInstance(320, 140, Image.SCALE_SMOOTH);
            lblIqImage.setIcon(new ImageIcon(img));
            lblIqImage.setText("");
        } else {
            lblIqImage.setIcon(null);
            String desc = (imageUrl != null ? "[" + imageUrl + "] " : "")
                    + (content != null ? content : "Quy luật logic");
            lblIqImage.setText("<html><center style='color:#2c3e50; font-family: Segoe UI;'>"
                    + "<div style='font-size:14px; margin-bottom:8px;'><b>🧩 HÌNH MINH HỌA QUY LUẬT LOGIC</b></div>"
                    + "<div style='font-size:12px; color:#7f8c8d;'>" + desc + "</div>"
                    + "<div style='font-size:18px; color:#e67e22; margin-top:10px;'><b>[ Bước 1: 0° ] ➜ [ Bước 2: 45° ] ➜ [ Bước 3: 90° ] ➜ [ ? ]</b></div>"
                    + "</center></html>");
        }
    }

    /**
     * Hiển thị kết quả chấm điểm IQ và cập nhật vào hồ sơ
     */
    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        if (result == null)
            return;
        btnSubmitIQ.setEnabled(true);

        if (result.isCorrect) {
            lblIqResult.setForeground(new Color(39, 174, 96));
            lblIqResult.setText("✅ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
        } else {
            lblIqResult.setForeground(new Color(231, 76, 60));
            lblIqResult.setText("❌ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
        }

        // Tự động đồng bộ điểm IQ hiển thị tại Sảnh (Lobby)
        mainFrame.getLobbyPanel().updateIQDisplay(result.currentIqScore);
    }
}
