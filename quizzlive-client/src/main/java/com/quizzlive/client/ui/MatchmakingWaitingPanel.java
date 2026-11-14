package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.client.util.IQImageUtil;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Giao diện Hàng đợi Ghép cặp tự động và Làm bài Test IQ bằng hình ảnh
 * Chuẩn phong cách NEO-BRUTALIST (Nền xám kem, Card trắng viền đen dày, bóng cứng)
 * Theo thiết kế SCREEN 4 trong master_gui_showcase.html
 * 
 * Phụ trách:
 * - Yêu cầu 2: Ghép cặp tự động trong 60 giây; hết thời gian chưa ghép được thì đưa về phòng chờ.
 * - Yêu cầu 11: Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để xếp hạng; điểm IQ lưu vào hồ sơ.
 */
public class MatchmakingWaitingPanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientMatchmakingService matchmakingService = new ClientMatchmakingService();
    private final ClientIQService iqService = new ClientIQService();

    // --- COMPONENTS YÊU CẦU 2 (HÀNG ĐỢI 60 GIÂY - THÀNH VIÊN 2) ---
    private RadarCirclePanel radarPanel;
    private JLabel lblCountdown;
    private JLabel lblQueueCriteria;
    private BrutalUI.BrutalProgressBar progressBar;
    private BrutalUI.BrutalButton btnCancelQueue;
    private Integer currentTopicId;
    private String currentDifficulty;
    private Timer queueTimer;
    private int remainingSeconds = 60;
    private boolean isTimeoutHandled = false;

    // --- COMPONENTS YÊU CẦU 11 (TEST IQ BẰNG HÌNH ẢNH) ---
    private JLabel lblIqTitle;
    private JLabel lblIqMeta;
    private JPanel pnlIqQuestionBox;
    private JLabel lblIqQuestionText;
    private JPanel pnlIqImageContainer;
    private JLabel lblIqImage;
    private JRadioButton rbOptA, rbOptB, rbOptC, rbOptD;
    private JPanel pnlOptA, pnlOptB, pnlOptC, pnlOptD;
    private ButtonGroup bgIqOptions;
    private BrutalUI.BrutalButton btnSubmitIQ;
    private BrutalUI.BrutalButton btnNextIQ;
    private JLabel lblIqResult;
    private Long currentIqQuestionId;

    public MatchmakingWaitingPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(24, 24));
        setBackground(BrutalUI.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // =========================================================================
        // 1. CỘT TRÁI: HÀNG ĐỢI GHÉP CẶP 60S (YÊU CẦU 2 - THÀNH VIÊN 2)
        // =========================================================================
        BrutalUI.CardPanel leftCard = new BrutalUI.CardPanel(16, 4);
        leftCard.setPreferredSize(new Dimension(320, 0));
        leftCard.setLayout(new BorderLayout(15, 15));
        leftCard.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JPanel leftCenterContent = new JPanel();
        leftCenterContent.setOpaque(false);
        leftCenterContent.setLayout(new BoxLayout(leftCenterContent, BoxLayout.Y_AXIS));

        // Radar Circular Timer
        radarPanel = new RadarCirclePanel();
        radarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblQueueTitle = new JLabel("ĐANG TÌM ĐỐI THỦ...");
        lblQueueTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblQueueTitle.setForeground(BrutalUI.COLOR_BLACK);
        lblQueueTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblQueueDesc = new JLabel("<html><center style='color:#71717A; font-size:11px;'>"
                + "Hệ thống đang quét người chơi có cùng trình độ trong hàng đợi 60s."
                + "</center></html>");
        lblQueueDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQueueDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCountdown = new JLabel("Thời gian chờ: 60s");
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCountdown.setForeground(new Color(225, 29, 72));
        lblCountdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar = new BrutalUI.BrutalProgressBar(0, 60);
        progressBar.setValue(60);
        progressBar.setMaximumSize(new Dimension(260, 16));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblQueueCriteria = new JLabel("🎯 Mục tiêu: Tất cả chủ đề   |   ⚡ Mọi cấp độ", SwingConstants.CENTER);
        lblQueueCriteria.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQueueCriteria.setForeground(BrutalUI.COLOR_MUTED);
        lblQueueCriteria.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftCenterContent.add(radarPanel);
        leftCenterContent.add(Box.createVerticalStrut(14));
        leftCenterContent.add(lblQueueTitle);
        leftCenterContent.add(Box.createVerticalStrut(6));
        leftCenterContent.add(lblQueueDesc);
        leftCenterContent.add(Box.createVerticalStrut(10));
        leftCenterContent.add(lblQueueCriteria);
        leftCenterContent.add(Box.createVerticalStrut(10));
        leftCenterContent.add(lblCountdown);
        leftCenterContent.add(Box.createVerticalStrut(12));
        leftCenterContent.add(progressBar);

        leftCard.add(leftCenterContent, BorderLayout.CENTER);

        // Nút Hủy Tìm Kiếm (Neo-Brutalist Danger Button)
        btnCancelQueue = new BrutalUI.BrutalButton(
                "✖ HỦY TÌM KIẾM & VỀ SẢNH",
                BrutalUI.BrutalButton.Style.DANGER
        );
        btnCancelQueue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelQueue.setPreferredSize(new Dimension(0, 44));
        btnCancelQueue.addActionListener(e -> cancelQueueAndReturn());
        leftCard.add(btnCancelQueue, BorderLayout.SOUTH);

        add(leftCard, BorderLayout.WEST);

        // =========================================================================
        // 2. CỘT PHẢI: MINI-GAME TEST IQ HÌNH ẢNH TRONG LÚC CHỜ (YÊU CẦU 11)
        // =========================================================================
        BrutalUI.CardPanel rightCard = new BrutalUI.CardPanel(16, 4);
        rightCard.setLayout(new BorderLayout(15, 14));
        rightCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Card IQ (Neo-Brutalist)
        JPanel iqHeader = new JPanel(new BorderLayout(10, 8));
        iqHeader.setOpaque(false);

        JPanel iqHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iqHeaderLeft.setOpaque(false);
        JLabel lblIqBadge = BrutalUI.createBadge("🧠 BÀI TEST IQ", new Color(254, 240, 138), BrutalUI.COLOR_BLACK);
        lblIqTitle = new JLabel("Bài Test IQ Trong Ngày (5 câu/ngày)");
        lblIqTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIqTitle.setForeground(BrutalUI.COLOR_BLACK);
        iqHeaderLeft.add(lblIqBadge);
        iqHeaderLeft.add(lblIqTitle);

        JLabel lblIqSub = new JLabel("<html><span style='color:#71717A; font-size:11px;'>Mỗi ngày làm 5 câu. Tổng 1.0 điểm IQ, mỗi câu sai trừ 0.2 điểm. Kết toán sau 5 câu.</span></html>");
        lblIqMeta = new JLabel("Đang tải câu hỏi từ cơ sở dữ liệu...");
        lblIqMeta.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblIqMeta.setForeground(new Color(37, 99, 235));

        JPanel iqHeaderBottom = new JPanel(new GridLayout(2, 1, 0, 2));
        iqHeaderBottom.setOpaque(false);
        iqHeaderBottom.add(lblIqSub);
        iqHeaderBottom.add(lblIqMeta);

        iqHeader.add(iqHeaderLeft, BorderLayout.NORTH);
        iqHeader.add(iqHeaderBottom, BorderLayout.SOUTH);
        rightCard.add(iqHeader, BorderLayout.NORTH);

        // Center IQ: Câu hỏi (Khung Neo-Brutalist) + 4 Options
        JPanel iqCenter = new JPanel(new BorderLayout(12, 12));
        iqCenter.setOpaque(false);

        // Khung câu hỏi IQ Neo-Brutalist
        pnlIqQuestionBox = new JPanel(new BorderLayout(10, 10));
        pnlIqQuestionBox.setBackground(Color.WHITE);
        pnlIqQuestionBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Text câu hỏi
        lblIqQuestionText = new JLabel("Đang nạp câu hỏi IQ...");
        lblIqQuestionText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIqQuestionText.setForeground(BrutalUI.COLOR_BLACK);
        lblIqQuestionText.setVerticalAlignment(SwingConstants.TOP);
        pnlIqQuestionBox.add(lblIqQuestionText, BorderLayout.NORTH);

        // Khung ảnh quy luật IQ (chỉ hiển thị khi câu hỏi có ảnh)
        pnlIqImageContainer = new JPanel(new BorderLayout());
        pnlIqImageContainer.setBackground(new Color(250, 250, 252));
        pnlIqImageContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 225), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        pnlIqImageContainer.setPreferredSize(new Dimension(0, 180));
        pnlIqImageContainer.setVisible(false);

        lblIqImage = new JLabel("", SwingConstants.CENTER);
        lblIqImage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIqImage.setForeground(BrutalUI.COLOR_BLACK);
        pnlIqImageContainer.add(lblIqImage, BorderLayout.CENTER);
        pnlIqQuestionBox.add(pnlIqImageContainer, BorderLayout.CENTER);

        iqCenter.add(pnlIqQuestionBox, BorderLayout.CENTER);

        // 4 lựa chọn đáp án IQ theo lưới 2x2
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        optionsPanel.setOpaque(false);

        rbOptA = createRadio();
        rbOptB = createRadio();
        rbOptC = createRadio();
        rbOptD = createRadio();

        bgIqOptions = new ButtonGroup();
        bgIqOptions.add(rbOptA);
        bgIqOptions.add(rbOptB);
        bgIqOptions.add(rbOptC);
        bgIqOptions.add(rbOptD);

        pnlOptA = wrapRadioInCard(rbOptA, "A. Đáp án A");
        pnlOptB = wrapRadioInCard(rbOptB, "B. Đáp án B");
        pnlOptC = wrapRadioInCard(rbOptC, "C. Đáp án C");
        pnlOptD = wrapRadioInCard(rbOptD, "D. Đáp án D");

        optionsPanel.add(pnlOptA);
        optionsPanel.add(pnlOptB);
        optionsPanel.add(pnlOptC);
        optionsPanel.add(pnlOptD);
        iqCenter.add(optionsPanel, BorderLayout.SOUTH);

        rightCard.add(iqCenter, BorderLayout.CENTER);

        // Footer IQ: Kết quả + Nút đổi câu + Nút Nộp bài (Neo-Brutalist)
        JPanel iqFooter = new JPanel(new BorderLayout(12, 0));
        iqFooter.setOpaque(false);

        lblIqResult = new JLabel("ℹ Điểm IQ được lưu vào hồ sơ cá nhân và không ảnh hưởng đến điểm thi đấu.");
        lblIqResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIqResult.setForeground(new Color(113, 113, 122));
        iqFooter.add(lblIqResult, BorderLayout.CENTER);

        JPanel iqActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        iqActions.setOpaque(false);

        btnNextIQ = new BrutalUI.BrutalButton("Đổi câu khác", BrutalUI.BrutalButton.Style.OUTLINE);
        btnNextIQ.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNextIQ.addActionListener(e -> {
            lblIqResult.setText("");
            iqService.requestIQQuestion();
        });

        btnSubmitIQ = new BrutalUI.BrutalButton("Nộp Đáp Án IQ", BrutalUI.BrutalButton.Style.PRIMARY);
        btnSubmitIQ.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmitIQ.addActionListener(e -> handleSubmitIQ());

        iqActions.add(btnNextIQ);
        iqActions.add(btnSubmitIQ);
        iqFooter.add(iqActions, BorderLayout.EAST);

        rightCard.add(iqFooter, BorderLayout.SOUTH);

        add(rightCard, BorderLayout.CENTER);
    }

    private JRadioButton createRadio() {
        JRadioButton rb = new JRadioButton();
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JPanel wrapRadioInCard(JRadioButton rb, String initialText) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        rb.setText(initialText);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rb.setForeground(BrutalUI.COLOR_BLACK);

        card.add(rb, BorderLayout.CENTER);

        // Click vào thẻ cũng chọn radio
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (rb.isEnabled()) {
                    rb.setSelected(true);
                }
            }
        });

        return card;
    }

    // =========================================================================
    // LOGIC HÀNG ĐỢI 60S (YÊU CẦU 2 - THÀNH VIÊN 2)
    // =========================================================================

    /**
     * Bắt đầu đếm ngược 60 giây khi người dùng bấm Tìm trận (Yêu cầu 2, 4)
     */
    public void startCountdown(int seconds) {
        startCountdown(seconds, "Tất cả chủ đề", "Mọi cấp độ", null, "ALL");
    }

    public void startCountdown(int seconds, String topicName, String diffName) {
        startCountdown(seconds, topicName, diffName, null, "ALL");
    }

    public void startCountdown(int seconds, String topicName, String diffName, Integer topicId, String difficulty) {
        this.isTimeoutHandled = false;
        this.currentTopicId = topicId;
        this.currentDifficulty = difficulty;
        this.remainingSeconds = seconds;

        if (progressBar != null) {
            progressBar.setMaximum(seconds);
            progressBar.setValue(seconds);
        }
        if (lblCountdown != null) {
            lblCountdown.setText("Thời gian chờ: " + remainingSeconds + "s");
        }
        if (radarPanel != null) {
            radarPanel.setSeconds(remainingSeconds);
        }

        String t = (topicName != null && !topicName.trim().isEmpty()) ? topicName : "Tất cả chủ đề";
        String d = (diffName != null && !diffName.trim().isEmpty()) ? diffName : "Mọi cấp độ";
        lblQueueCriteria.setText("🎯 Mục tiêu: " + t + "   |   ⚡ Độ khó: " + d);

        if (queueTimer != null && queueTimer.isRunning()) {
            queueTimer.stop();
        }

        queueTimer = new Timer(1000, e -> {
            remainingSeconds--;
            if (lblCountdown != null) {
                lblCountdown.setText("Thời gian chờ: " + remainingSeconds + "s");
            }
            if (progressBar != null) {
                progressBar.setValue(remainingSeconds);
            }
            if (radarPanel != null) {
                radarPanel.setSeconds(remainingSeconds);
            }

            if (remainingSeconds <= 0) {
                queueTimer.stop();
                handleQueueTimeout();
            }
        });
        queueTimer.start();

        // Gửi yêu cầu lấy 1 câu đố IQ trích xuất theo loại IQ_TEST
        lblIqResult.setText("");
        iqService.requestIQQuestion();
    }

    public void stopCountdown() {
        if (queueTimer != null) {
            queueTimer.stop();
        }
    }

    private void cancelQueueAndReturn() {
        this.isTimeoutHandled = true;
        stopCountdown();
        matchmakingService.leaveQueue();
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    /**
     * Xử lý khi hết 60 giây chưa tìm thấy đối thủ (Yêu cầu 2)
     */
    public void handleQueueTimeout() {
        if (isTimeoutHandled) return;
        isTimeoutHandled = true;
        stopCountdown();
        JOptionPane.showMessageDialog(this,
                "Hết thời gian 60 giây chưa tìm thấy đối thủ phù hợp!\nĐưa người chơi về phòng chờ.",
                "Thông báo ghép cặp (YC 2)", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    // =========================================================================
    // LOGIC TEST IQ TRONG NGÀY (5 CÂU/NGÀY - KẾT TOÁN SAU 5 CÂU)
    // =========================================================================

    private void handleSubmitIQ() {
        if (currentIqQuestionId == null || currentIqQuestionId <= 0) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi IQ nào đang mở!");
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
        rbOptA.setEnabled(false);
        rbOptB.setEnabled(false);
        rbOptC.setEnabled(false);
        rbOptD.setEnabled(false);
        lblIqResult.setForeground(Color.GRAY);
        lblIqResult.setText("Đang gửi đáp án...");

        iqService.submitIQAnswer(currentIqQuestionId, selectedAnswerId);
    }

    /**
     * Hiển thị câu hỏi IQ loại IQ_TEST nhận được từ Server
     */
    public void renderIQQuestion(IQTestDTO.IQQuestionNotify question) {
        if (question == null) return;
        try {
            this.currentIqQuestionId = question.questionId;

            if (question.isDailyCompleted || question.questionId == -1L) {
                // Đã hoàn thành 5/5 câu hỏi IQ trong ngày hôm nay
                if (lblIqTitle != null) lblIqTitle.setText("🧠 BÀI TEST IQ TRONG NGÀY (5 CÂU/NGÀY)");
                if (lblIqMeta != null) {
                    lblIqMeta.setText("Trạng thái: " + (question.dailyMessage != null ? question.dailyMessage : "Đã hoàn thành 5/5 câu hỏi hôm nay"));
                }
                
                if (pnlIqQuestionBox != null) {
                    pnlIqQuestionBox.remove(lblIqQuestionText);
                    pnlIqQuestionBox.remove(pnlIqImageContainer);
                }
                if (pnlIqImageContainer != null) pnlIqImageContainer.setVisible(false);
                if (lblIqImage != null) {
                    lblIqImage.setIcon(null);
                    lblIqImage.setText("");
                }

                if (lblIqQuestionText != null) {
                    lblIqQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 14px; font-weight: bold; color: #16a34a; line-height: 1.5; padding: 16px; text-align: center;'>"
                            + (question.content != null ? question.content.replace("\n", "<br>") : "Hôm nay bạn đã hoàn thành đủ 5 câu hỏi IQ. Hãy quay lại vào ngày mai!")
                            + "</div></html>");
                    if (pnlIqQuestionBox != null) pnlIqQuestionBox.add(lblIqQuestionText, BorderLayout.CENTER);
                }

                if (lblIqResult != null) {
                    lblIqResult.setText("Hôm nay bạn đã hoàn thành bài Test IQ. Vui lòng quay lại vào ngày mai!");
                    lblIqResult.setForeground(new Color(22, 163, 74));
                }
                if (btnSubmitIQ != null) {
                    btnSubmitIQ.setEnabled(false);
                    btnSubmitIQ.setText("Đã hoàn thành hôm nay");
                }
                if (btnNextIQ != null) btnNextIQ.setVisible(false);
                if (pnlOptA != null) pnlOptA.setVisible(false);
                if (pnlOptB != null) pnlOptB.setVisible(false);
                if (pnlOptC != null) pnlOptC.setVisible(false);
                if (pnlOptD != null) pnlOptD.setVisible(false);

                if (pnlIqQuestionBox != null) {
                    pnlIqQuestionBox.revalidate();
                    pnlIqQuestionBox.repaint();
                }
                return;
            }

            int idx = question.currentQuestionIndex > 0 ? question.currentQuestionIndex : 1;
            int max = question.maxDailyQuestions > 0 ? question.maxDailyQuestions : 5;

            if (lblIqTitle != null) {
                lblIqTitle.setText("Bài Test IQ Trong Ngày - Câu " + idx + "/" + max);
            }
            String topicStr = question.topicName != null ? question.topicName : "Tư duy IQ & Logic";
            String diffStr = question.difficulty != null ? question.difficulty : "MEDIUM";
            if (lblIqMeta != null) {
                lblIqMeta.setText("📌 Câu " + idx + "/" + max + " trong ngày • Mã #" + question.questionId + " | Chủ đề: " + topicStr + " | Cấp độ: " + diffStr);
            }

            if (btnSubmitIQ != null) {
                btnSubmitIQ.setEnabled(true);
                btnSubmitIQ.setText("Nộp & Chuyển câu tiếp ➡️");
            }
            if (btnNextIQ != null) btnNextIQ.setVisible(false); // Không bỏ qua tùy tiện vì giới hạn 5 câu
            if (bgIqOptions != null) bgIqOptions.clearSelection();

            List<BattleDTO.QuestionAnswerOption> options = question.options;
            JRadioButton[] rbs = new JRadioButton[] { rbOptA, rbOptB, rbOptC, rbOptD };
            JPanel[] pnls = new JPanel[] { pnlOptA, pnlOptB, pnlOptC, pnlOptD };

            for (int i = 0; i < rbs.length; i++) {
                if (rbs[i] != null && pnls[i] != null) {
                    if (options != null && i < options.size()) {
                        BattleDTO.QuestionAnswerOption opt = options.get(i);
                        rbs[i].setText(opt.key + ". " + (opt.content != null ? opt.content : ""));
                        rbs[i].putClientProperty("answerId", opt.answerId);
                        pnls[i].setVisible(true);
                        rbs[i].setEnabled(true);
                    } else {
                        pnls[i].setVisible(false);
                    }
                }
            }

            // Logic hiển thị: Nếu câu hỏi không có ảnh thì CHỈ hiển thị nội dung; Nếu có ảnh thì hiển thị CẢ 2 (nội dung + ảnh)
            boolean hasImage = (question.imageUrl != null && !question.imageUrl.trim().isEmpty());
            if (pnlIqQuestionBox != null) {
                pnlIqQuestionBox.remove(lblIqQuestionText);
                pnlIqQuestionBox.remove(pnlIqImageContainer);

                if (hasImage) {
                    if (lblIqQuestionText != null) {
                        lblIqQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 13px; font-weight: bold; color: #2c3e50; line-height: 1.4;'>"
                                + (question.content != null ? question.content : "") + "</div></html>");
                        pnlIqQuestionBox.add(lblIqQuestionText, BorderLayout.NORTH);
                    }
                    if (pnlIqImageContainer != null) {
                        pnlIqImageContainer.setVisible(true);
                        pnlIqQuestionBox.add(pnlIqImageContainer, BorderLayout.CENTER);
                        renderImagePuzzle(question.imageUrl, question.content);
                    }
                } else {
                    if (lblIqQuestionText != null) {
                        lblIqQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 15px; font-weight: bold; color: #1e293b; line-height: 1.6; padding: 12px;'>"
                                + (question.content != null ? question.content : "") + "</div></html>");
                        pnlIqQuestionBox.add(lblIqQuestionText, BorderLayout.CENTER);
                    }
                    if (pnlIqImageContainer != null) {
                        pnlIqImageContainer.setVisible(false);
                    }
                    if (lblIqImage != null) {
                        lblIqImage.setIcon(null);
                        lblIqImage.setText("");
                    }
                }

                pnlIqQuestionBox.revalidate();
                pnlIqQuestionBox.repaint();
            }
        } catch (Exception e) {
            System.err.println("[MatchmakingWaitingPanel] Lỗi render câu hỏi IQ: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Hiển thị kết quả chấm điểm IQ và kết toán điểm sau 5 câu
     */
    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        if (result == null) return;

        if (!result.isFinished) {
            // Đã trả lời 1 câu trong số 5 câu -> Server tự động chuyển câu tiếp theo
            lblIqResult.setForeground(result.isCorrect ? new Color(22, 163, 74) : new Color(220, 38, 38));
            lblIqResult.setText(result.message);
        } else {
            // ĐÃ HOÀN THÀNH VÀ KẾT TOÁN 5 CÂU HỎI TRONG NGÀY
            lblIqTitle.setText("🎉 ĐÃ HOÀN THÀNH TEST IQ HÔM NAY (5/5 CÂU)");
            lblIqResult.setForeground(new Color(22, 163, 74));
            lblIqResult.setText("<html><b style='color:#16a34a;'>Đã kết toán điểm IQ:</b> Đúng " + result.correctCount + "/5, Sai "
                    + result.wrongCount + "/5 (Cộng +" + String.format("%.1f", result.deltaIq) + " IQ). Điểm mới: <b>"
                    + String.format("%.1f", result.currentIqScore) + "</b></html>");
            btnSubmitIQ.setEnabled(false);
            btnSubmitIQ.setText("Đã hoàn thành 5/5 câu");

            if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
                mainFrame.getLobbyPanel().updateIQDisplay(result.currentIqScore);
            }

            JOptionPane.showMessageDialog(this,
                    result.message,
                    "Kết toán Điểm IQ hôm nay (5 câu)",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // INNER CLASS: VÒNG TRÒN ĐẾM NGƯỢC RADAR NEO-BRUTALIST
    // =========================================================================
    private static class RadarCirclePanel extends JPanel {
        private int seconds = 60;

        public RadarCirclePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(110, 110));
            setMaximumSize(new Dimension(110, 110));
        }

        public void setSeconds(int sec) {
            this.seconds = sec;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int size = 96;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Đổ bóng đen cứng 4px
            g2.setColor(BrutalUI.COLOR_BLACK);
            g2.fillOval(x + 4, y + 4, size, size);

            // Nền tròn trắng
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, size, size);

            // Viền đen 3px
            g2.setColor(BrutalUI.COLOR_BLACK);
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(x, y, size, size);

            // Vẽ số giây (đổi màu đỏ khi thời gian gấp gáp)
            if (seconds <= 10) {
                g2.setColor(new Color(225, 29, 72));
            } else if (seconds <= 25) {
                g2.setColor(new Color(234, 88, 12));
            } else {
                g2.setColor(BrutalUI.COLOR_BLACK);
            }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
            FontMetrics fm = g2.getFontMetrics();
            String secStr = String.valueOf(seconds);
            int textX = x + (size - fm.stringWidth(secStr)) / 2;
            int textY = y + (size / 2) + 5;
            g2.drawString(secStr, textX, textY);

            // Vẽ nhãn GIÂY
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(new Color(113, 113, 122));
            FontMetrics fmSmall = g2.getFontMetrics();
            String unitStr = "GIÂY";
            int unitX = x + (size - fmSmall.stringWidth(unitStr)) / 2;
            g2.drawString(unitStr, unitX, textY + 16);

            g2.dispose();
        }
    }
}
