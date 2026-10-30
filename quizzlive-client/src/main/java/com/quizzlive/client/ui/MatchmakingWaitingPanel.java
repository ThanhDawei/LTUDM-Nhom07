package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.shared.dto.IQTestDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Hàng đợi Ghép cặp tự động và Làm bài Test IQ bằng hình ảnh
 * Phụ trách:
 * - Yêu cầu 2: "Ghép cặp tự động trong 60 giây; hết thời gian chưa ghép được thì đưa về phòng chờ"
 * - Yêu cầu 11: "Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để xếp hạng; điểm IQ lưu vào hồ sơ"
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

    // Components cho Yêu cầu 11 (Test IQ)
    private JLabel lblIqTitle;
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
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

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

        // Khung hiển thị ảnh IQ
        lblIqImage = new JLabel("[ Hình ảnh câu hỏi IQ ]", SwingConstants.CENTER);
        lblIqImage.setPreferredSize(new Dimension(350, 180));
        lblIqImage.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY));
        lblIqImage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        iqCard.add(lblIqImage, BorderLayout.CENTER);

        // Các lựa chọn đáp án IQ
        JPanel iqBottom = new JPanel(new BorderLayout(10, 10));
        iqBottom.setOpaque(false);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionsPanel.setOpaque(false);
        rbOptA = new JRadioButton("Đáp án A");
        rbOptB = new JRadioButton("Đáp án B");
        rbOptC = new JRadioButton("Đáp án C");
        rbOptD = new JRadioButton("Đáp án D");
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
        lblIqResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIqResult.setForeground(new Color(39, 174, 96));

        btnNextIQ = new JButton("Đổi câu khác");
        btnNextIQ.setFocusPainted(false);
        btnNextIQ.addActionListener(e -> iqService.requestIQQuestion());

        btnSubmitIQ = new JButton("Nộp Đáp Án IQ");
        btnSubmitIQ.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmitIQ.setBackground(new Color(142, 68, 173));
        btnSubmitIQ.setForeground(Color.WHITE);
        btnSubmitIQ.setFocusPainted(false);
        btnSubmitIQ.addActionListener(e -> handleSubmitIQ());

        iqActions.add(lblIqResult);
        iqActions.add(btnNextIQ);
        iqActions.add(btnSubmitIQ);
        iqBottom.add(iqActions, BorderLayout.SOUTH);

        iqCard.add(iqBottom, BorderLayout.SOUTH);
        add(iqCard, BorderLayout.CENTER);
    }

    /**
     * Bắt đầu đếm ngược 60 giây khi người dùng bấm Tìm trận
     */
    public void startCountdown(int seconds) {
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

        // Đồng thời gửi yêu cầu lấy 1 câu đố IQ hình ảnh để người chơi giải trí trong lúc chờ
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
        JOptionPane.showMessageDialog(this, "Hết thời gian 60 giây chưa tìm thấy đối thủ phù hợp!\nĐưa về phòng chờ.", "Thông báo ghép cặp", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    private void handleSubmitIQ() {
        if (currentIqQuestionId == null) {
            JOptionPane.showMessageDialog(this, "Chưa có câu hỏi IQ nào!");
            return;
        }

        long selectedAnswerId = -1;
        // TODO: Map selected radio button to answer id
        if (rbOptA.isSelected()) selectedAnswerId = 1;
        else if (rbOptB.isSelected()) selectedAnswerId = 2;
        else if (rbOptC.isSelected()) selectedAnswerId = 3;
        else if (rbOptD.isSelected()) selectedAnswerId = 4;

        if (selectedAnswerId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 đáp án!");
            return;
        }

        iqService.submitIQAnswer(currentIqQuestionId, selectedAnswerId);
    }

    public void renderIQQuestion(IQTestDTO.IQQuestionNotify question) {
        this.currentIqQuestionId = question.questionId;
        lblIqTitle.setText("🧠 " + (question.content != null ? question.content : "Quan sát hình ảnh và chọn đáp án đúng:"));
        lblIqImage.setText(question.imageUrl != null ? "Ảnh IQ: " + question.imageUrl : "[ Hình minh họa câu đố logic ]");
        lblIqResult.setText("");
        bgIqOptions.clearSelection();
    }

    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        lblIqResult.setText(result.isCorrect ? "✅ Chính xác! Điểm IQ hiện tại: " + result.currentIqScore : "❌ Chưa chính xác! Điểm IQ: " + result.currentIqScore);
    }
}
