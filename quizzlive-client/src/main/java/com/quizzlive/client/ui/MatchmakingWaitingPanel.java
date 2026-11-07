package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.service.ClientMatchmakingService;
import com.quizzlive.client.util.BrutalUI;
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
    private BrutalUI.BrutalProgressBar progressBar;
    private BrutalUI.BrutalButton btnCancelQueue;
    private Timer queueTimer;
    private int remainingSeconds = 60;
    private boolean isTimeoutHandled = false;

    // --- COMPONENTS YÊU CẦU 11 (TEST IQ BẰNG HÌNH ẢNH) ---
    private JLabel lblIqTitle;
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

        leftCenterContent.add(radarPanel);
        leftCenterContent.add(Box.createVerticalStrut(14));
        leftCenterContent.add(lblQueueTitle);
        leftCenterContent.add(Box.createVerticalStrut(6));
        leftCenterContent.add(lblQueueDesc);
        leftCenterContent.add(Box.createVerticalStrut(12));
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

        // Header Card IQ
        JPanel iqHeader = new JPanel(new BorderLayout(10, 8));
        iqHeader.setOpaque(false);

        JPanel iqHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iqHeaderLeft.setOpaque(false);
        JLabel badgeYC11 = BrutalUI.createBadge("YÊU CẦU 11", BrutalUI.COLOR_INFO_BG, BrutalUI.COLOR_INFO_TEXT);
        JLabel lblIqHeaderTitle = new JLabel("Câu Hỏi Thử Trí IQ Trong Lúc Chờ Ghép");
        lblIqHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIqHeaderTitle.setForeground(BrutalUI.COLOR_BLACK);
        iqHeaderLeft.add(badgeYC11);
        iqHeaderLeft.add(lblIqHeaderTitle);

        JLabel badgeDiff = BrutalUI.createBadge("Độ khó: Vừa", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);

        iqHeader.add(iqHeaderLeft, BorderLayout.WEST);
        iqHeader.add(badgeDiff, BorderLayout.EAST);

        // Dòng phân cách Neo-Brutalist
        JPanel headerWrapper = new JPanel(new BorderLayout(0, 10));
        headerWrapper.setOpaque(false);
        headerWrapper.add(iqHeader, BorderLayout.NORTH);

        JSeparator sep = new JSeparator();
        sep.setForeground(BrutalUI.COLOR_BLACK);
        headerWrapper.add(sep, BorderLayout.SOUTH);
        rightCard.add(headerWrapper, BorderLayout.NORTH);

        // Nội dung chính IQ: Tiêu đề câu hỏi, khung ảnh và 4 đáp án
        JPanel iqCenter = new JPanel(new BorderLayout(12, 12));
        iqCenter.setOpaque(false);

        lblIqTitle = new JLabel("Quan sát quy luật xoay của hình bên dưới và chọn đáp án thích hợp:");
        lblIqTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIqTitle.setForeground(new Color(39, 39, 42));
        iqCenter.add(lblIqTitle, BorderLayout.NORTH);

        // Khung hiển thị câu đố IQ hình ảnh
        pnlIqImageContainer = new JPanel(new BorderLayout());
        pnlIqImageContainer.setBackground(new Color(250, 250, 252));
        pnlIqImageContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        pnlIqImageContainer.setPreferredSize(new Dimension(0, 180));

        lblIqImage = new JLabel("[ Đang nạp câu đố IQ hình ảnh... ]", SwingConstants.CENTER);
        lblIqImage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIqImage.setForeground(BrutalUI.COLOR_BLACK);
        pnlIqImageContainer.add(lblIqImage, BorderLayout.CENTER);
        iqCenter.add(pnlIqImageContainer, BorderLayout.CENTER);

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

        // Footer IQ: Kết quả + Nút đổi câu + Nút Nộp bài
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
     * Bắt đầu đếm ngược 60 giây khi người dùng bấm Tìm trận
     */
    public void startCountdown(int seconds) {
        this.isTimeoutHandled = false;
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

        // Gửi yêu cầu lấy 1 câu đố IQ hình ảnh để giải trí trong lúc chờ
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
    // LOGIC TEST IQ HÌNH ẢNH (YÊU CẦU 11)
    // =========================================================================

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
        if (question == null) return;
        this.currentIqQuestionId = question.questionId;
        lblIqTitle.setText("🧠 " + (question.content != null ? question.content : "Quan sát hình ảnh và chọn đáp án đúng:"));
        lblIqResult.setText("");
        btnSubmitIQ.setEnabled(true);
        bgIqOptions.clearSelection();

        List<BattleDTO.QuestionAnswerOption> options = question.options;
        JRadioButton[] rbs = new JRadioButton[] { rbOptA, rbOptB, rbOptC, rbOptD };
        JPanel[] pnls = new JPanel[] { pnlOptA, pnlOptB, pnlOptC, pnlOptD };

        for (int i = 0; i < rbs.length; i++) {
            if (options != null && i < options.size()) {
                BattleDTO.QuestionAnswerOption opt = options.get(i);
                rbs[i].setText(opt.key + ". " + opt.content);
                rbs[i].putClientProperty("answerId", opt.answerId);
                pnls[i].setVisible(true);
                rbs[i].setEnabled(true);
            } else {
                pnls[i].setVisible(false);
            }
        }

        renderImagePuzzle(question.imageUrl, question.content);
    }

    /**
     * Hiển thị hình ảnh hoặc bản vẽ đồ họa mô phỏng quy luật logic của câu hỏi IQ
     */
    private void renderImagePuzzle(String imageUrl, String content) {
        if (imageUrl != null && new File(imageUrl).exists()) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image img = icon.getImage().getScaledInstance(340, 150, Image.SCALE_SMOOTH);
            lblIqImage.setIcon(new ImageIcon(img));
            lblIqImage.setText("");
        } else {
            lblIqImage.setIcon(null);
            String desc = (imageUrl != null ? "[" + imageUrl + "] " : "")
                    + (content != null ? content : "Quy luật logic");
            lblIqImage.setText("<html><center style='color:#09090B; font-family: Segoe UI;'>"
                    + "<div style='font-size:13px; margin-bottom:6px;'><b>🧩 HÌNH MINH HỌA QUY LUẬT LOGIC</b></div>"
                    + "<div style='font-size:11px; color:#71717A;'>" + desc + "</div>"
                    + "<div style='font-size:16px; color:#EA580C; margin-top:10px;'><b>[ Bước 1: 0° ] ➜ [ Bước 2: 45° ] ➜ [ Bước 3: 90° ] ➜ [ ? ]</b></div>"
                    + "</center></html>");
        }
    }

    /**
     * Hiển thị kết quả chấm điểm IQ và cập nhật vào hồ sơ
     */
    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        if (result == null) return;
        btnSubmitIQ.setEnabled(true);

        if (result.isCorrect) {
            lblIqResult.setForeground(new Color(22, 163, 74));
            lblIqResult.setText("✅ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
        } else {
            lblIqResult.setForeground(new Color(220, 38, 38));
            lblIqResult.setText("❌ " + result.message + " (Điểm IQ: " + result.currentIqScore + ")");
        }

        if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
            mainFrame.getLobbyPanel().updateIQDisplay(result.currentIqScore);
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
