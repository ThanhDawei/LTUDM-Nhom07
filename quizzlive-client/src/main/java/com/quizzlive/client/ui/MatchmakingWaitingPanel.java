package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.IQImageUtil;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Giao diện Hàng đợi Ghép cặp tự động và Làm bài Test IQ bằng hình ảnh
 * Phụ trách:
 * - Yêu cầu 2: "Ghép cặp tự động trong 60 giây; hết thời gian chưa ghép được thì đưa về phòng chờ"
 * - Yêu cầu 11: "Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để xếp hạng; điểm IQ lưu vào hồ sơ"
 * Cấu hình: Không dùng băm và mã hóa.
 */
public class MatchmakingWaitingPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientMatchmakingService matchmakingService = new ClientMatchmakingService();
    private final ClientIQService iqService = new ClientIQService();

    // Components cho Yêu cầu 2 (Đếm ngược 60s)
    private JLabel lblCountdown;
    private JLabel lblQueueCriteria;
    private JProgressBar progressBar;
    private JButton btnCancelQueue;
    private Timer queueTimer;
    private int remainingSeconds = 60;

    // Components cho Yêu cầu 11 (Test IQ bằng hình ảnh)
    private JLabel lblIqTitle;
    private JLabel lblIqMeta;
    private JLabel lblIqQuestionText;
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
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel queueInfo = new JPanel(new GridLayout(3, 1, 3, 3));
        queueInfo.setOpaque(false);
        JLabel lblQueueHeader = new JLabel("ĐANG TÌM KIẾM ĐỐI THỦ THÍCH HỢP...", SwingConstants.CENTER);
        lblQueueHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQueueHeader.setForeground(new Color(41, 128, 185));

        lblQueueCriteria = new JLabel("🎯 Mục tiêu: Tất cả chủ đề  |  Mọi cấp độ", SwingConstants.CENTER);
        lblQueueCriteria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQueueCriteria.setForeground(new Color(127, 140, 141));

        lblCountdown = new JLabel("Thời gian chờ: 60s", SwingConstants.CENTER);
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCountdown.setForeground(new Color(231, 76, 60));

        queueInfo.add(lblQueueHeader);
        queueInfo.add(lblQueueCriteria);
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
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JPanel iqHeader = new JPanel(new GridLayout(2, 1, 2, 2));
        iqHeader.setOpaque(false);
        lblIqTitle = new JLabel("🧠 THỬ TÀI IQ TRONG LÚC CHỜ GHÉP CẶP (YÊU CẦU 11)");
        lblIqTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIqTitle.setForeground(new Color(142, 68, 173));

        lblIqMeta = new JLabel("Câu hỏi trích xuất trực tiếp từ Cơ sở dữ liệu • Điểm IQ lưu vào hồ sơ cá nhân");
        lblIqMeta.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblIqMeta.setForeground(Color.GRAY);

        iqHeader.add(lblIqTitle);
        iqHeader.add(lblIqMeta);
        iqCard.add(iqHeader, BorderLayout.NORTH);

        // Khung hiển thị nội dung câu hỏi và hình ảnh từ CSDL
        JPanel centerQuestionPanel = new JPanel(new BorderLayout(8, 8));
        centerQuestionPanel.setOpaque(false);

        lblIqQuestionText = new JLabel("Đang nạp câu hỏi từ Cơ sở dữ liệu...", SwingConstants.LEFT);
        lblIqQuestionText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIqQuestionText.setForeground(new Color(44, 62, 80));
        lblIqQuestionText.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        centerQuestionPanel.add(lblIqQuestionText, BorderLayout.NORTH);

        pnlIqImageContainer = new JPanel(new BorderLayout());
        pnlIqImageContainer.setBackground(new Color(250, 250, 252));
        pnlIqImageContainer.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 230), 1));
        pnlIqImageContainer.setPreferredSize(new Dimension(380, 150));

        lblIqImage = new JLabel("[ Đang nạp hình ảnh minh họa từ CSDL... ]", SwingConstants.CENTER);
        lblIqImage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIqImage.setForeground(new Color(52, 73, 94));
        pnlIqImageContainer.add(lblIqImage, BorderLayout.CENTER);
        centerQuestionPanel.add(pnlIqImageContainer, BorderLayout.CENTER);

        iqCard.add(centerQuestionPanel, BorderLayout.CENTER);

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
     * Bắt đầu đếm ngược 60 giây khi người dùng bấm Tìm trận (Yêu cầu 2, 4)
     */
    public void startCountdown(int seconds) {
        startCountdown(seconds, "Tất cả chủ đề", "Mọi cấp độ");
    }

    public void startCountdown(int seconds, String topicName, String diffName) {
        this.remainingSeconds = seconds;
        progressBar.setMaximum(seconds);
        progressBar.setValue(seconds);
        lblCountdown.setText("Thời gian chờ: " + remainingSeconds + "s");

        String t = (topicName != null && !topicName.trim().isEmpty()) ? topicName : "Tất cả chủ đề";
        String d = (diffName != null && !diffName.trim().isEmpty()) ? diffName : "Mọi cấp độ";
        lblQueueCriteria.setText("🎯 Mục tiêu: " + t + "   |   ⚡ Độ khó: " + d);

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
        stopCountdown();
        matchmakingService.leaveQueue();
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    /**
     * Xử lý khi hết 60 giây chưa tìm thấy đối thủ (Yêu cầu 2)
     */
    public void handleQueueTimeout() {
        stopCountdown();
        JOptionPane.showMessageDialog(this, "Hết thời gian 60 giây chưa tìm thấy đối thủ phù hợp!\nĐưa người chơi về phòng chờ.", "Thông báo ghép cặp (YC 2)", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    private void handleSubmitIQ() {
        if (currentIqQuestionId == null) {
            JOptionPane.showMessageDialog(this, "Chưa có câu hỏi IQ nào!");
            return;
        }

        Long selectedAnswerId = null;
        if (rbOptA.isSelected()) selectedAnswerId = (Long) rbOptA.getClientProperty("answerId");
        else if (rbOptB.isSelected()) selectedAnswerId = (Long) rbOptB.getClientProperty("answerId");
        else if (rbOptC.isSelected()) selectedAnswerId = (Long) rbOptC.getClientProperty("answerId");
        else if (rbOptD.isSelected()) selectedAnswerId = (Long) rbOptD.getClientProperty("answerId");

        if (selectedAnswerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 đáp án trước khi nộp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        if (question == null) return;
        this.currentIqQuestionId = question.questionId;

        if (question.questionId == -1L) {
            // Đã hoàn thành tất cả câu đố IQ trong ngân hàng câu hỏi
            lblIqMeta.setText("Trạng thái: Đã hoàn thành toàn bộ câu hỏi trong CSDL");
            lblIqQuestionText.setText("<html><div style='font-size: 14px; font-weight: bold; color: #27ae60;'>" + question.content + "</div></html>");
            lblIqResult.setText("Bạn đã xuất sắc hoàn thành tất cả câu đố IQ!");
            lblIqResult.setForeground(new Color(39, 174, 96));
            btnSubmitIQ.setEnabled(false);
            btnNextIQ.setEnabled(false);
            rbOptA.setVisible(false);
            rbOptB.setVisible(false);
            rbOptC.setVisible(false);
            rbOptD.setVisible(false);
            renderImagePuzzle(question.imageUrl, question.content);
            return;
        }

        String topicStr = question.topicName != null ? question.topicName : "Tư duy IQ & Kiến thức";
        String diffStr = question.difficulty != null ? question.difficulty : "MEDIUM";
        lblIqMeta.setText("📌 Câu hỏi từ Cơ sở dữ liệu • Mã #" + question.questionId + " | Chủ đề: " + topicStr + " | Cấp độ: " + diffStr);
        lblIqQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 13px; font-weight: bold; color: #2c3e50; line-height: 1.4;'>"
                + question.content + "</div></html>");
        lblIqResult.setText("");
        btnSubmitIQ.setEnabled(true);
        btnSubmitIQ.setText("Nộp Đáp Án IQ");
        btnNextIQ.setText("Đổi câu khác");
        btnNextIQ.setEnabled(true);
        bgIqOptions.clearSelection();

        // Gán đáp án vào các radio button
        List<BattleDTO.QuestionAnswerOption> options = question.options;
        JRadioButton[] rbs = new JRadioButton[]{rbOptA, rbOptB, rbOptC, rbOptD};
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

        // Cập nhật khung hình ảnh trực quan sắc nét
        renderImagePuzzle(question.imageUrl, question.content);
    }

    /**
     * Hiển thị hình ảnh hoặc bản vẽ đồ họa mô phỏng quy luật logic của câu hỏi IQ (Yêu cầu 11)
     */
    private void renderImagePuzzle(String imageUrl, String content) {
        int w = pnlIqImageContainer.getWidth() > 300 ? pnlIqImageContainer.getWidth() : 520;
        int h = pnlIqImageContainer.getHeight() > 100 ? pnlIqImageContainer.getHeight() : 155;
        ImageIcon icon = IQImageUtil.getIQImage(imageUrl, content, w, h);
        lblIqImage.setIcon(icon);
        lblIqImage.setText("");
    }

    /**
     * Hiển thị kết quả chấm điểm IQ và cập nhật vào hồ sơ
     */
    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        if (result == null) return;

        if (result.isCorrect) {
            lblIqResult.setForeground(new Color(39, 174, 96));
            lblIqResult.setText("✅ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
            btnSubmitIQ.setEnabled(false);
            btnSubmitIQ.setText("Đã hoàn thành");
            btnNextIQ.setText("Câu tiếp theo ➡️");
            // Khóa lựa chọn sau khi đã đúng
            rbOptA.setEnabled(false);
            rbOptB.setEnabled(false);
            rbOptC.setEnabled(false);
            rbOptD.setEnabled(false);
        } else {
            lblIqResult.setForeground(new Color(231, 76, 60));
            lblIqResult.setText("❌ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
            btnSubmitIQ.setEnabled(true);
            btnSubmitIQ.setText("Chọn lại & Nộp lại");
        }

        // Tự động đồng bộ điểm IQ hiển thị tại Sảnh (Lobby)
        mainFrame.getLobbyPanel().updateIQDisplay(result.currentIqScore);
    }
}
