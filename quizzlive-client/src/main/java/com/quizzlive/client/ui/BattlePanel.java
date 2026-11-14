package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientBattleService;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.shared.dto.BattleDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Trận đấu Realtime 1v1 - Chuẩn phong cách NEO-BRUTALIST (Nền trắng viền đen)
 * Theo thiết kế SCREEN 5, 6, 7 trong master_gui_showcase.html
 * 
 * Phụ trách:
 * - Yêu cầu 5: Mỗi trận 5 câu, đếm ngược 10 giây, tính điểm theo tốc độ phản hồi
 * - Yêu cầu 6: Hiển thị lựa chọn của đối thủ và đáp án đúng khi kết thúc lượt
 * - Yêu cầu 7: Hiển thị câu hỏi và đáp án xáo trộn từ server
 * - Yêu cầu 8: Tổng kết kết quả trận (sau 5 câu ai điểm cao hơn thắng)
 */
public class BattlePanel extends JPanel {
    private final MainClientFrame mainFrame;
    private final ClientBattleService battleService = new ClientBattleService();

    private long currentMatchId;
    private int currentQuestionIndex;
    private int timeRemaining = 10;
    private Timer questionTimer;
    private boolean answerSubmitted = false;
    private Long selectedAnswerId = null;

    // --- UI HEADER COMPONENTS ---
    private JLabel lblPlayer1Badge;
    private JLabel lblPlayer1Name;
    private JLabel lblPlayer1Score;


    private JLabel lblRoundBadge;
    private JLabel lblTimerText;
    private BrutalUI.BrutalProgressBar timerProgressBar;

    private JLabel lblPlayer2Badge;
    private JLabel lblPlayer2Name;
    private JLabel lblPlayer2Score;

    // --- UI QUESTION COMPONENTS ---
    private JLabel lblTopicBadge;
    private JTextArea txtQuestion;
    private JLabel lblQuestionImage;

    // --- UI ANSWERS (4 đáp án Neo-Brutalist) ---
    private BrutalUI.AnswerButton btnAnswerA;
    private BrutalUI.AnswerButton btnAnswerB;
    private BrutalUI.AnswerButton btnAnswerC;
    private BrutalUI.AnswerButton btnAnswerD;
    private List<BattleDTO.QuestionAnswerOption> currentOptions;

    // --- UI STATUS / FOOTER ---
    private JLabel lblStatus;
    private JLabel lblOpponentStatus;
    private BrutalUI.BrutalButton btnSurrender;

    public BattlePanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BrutalUI.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // =========================================================================
        // 1. TOP HEADER: Round Info, 2 Players Live Score, 10s Timer (Yêu cầu 5)
        // =========================================================================
        BrutalUI.CardPanel topCard = new BrutalUI.CardPanel(16, 3);
        topCard.setLayout(new BorderLayout(15, 8));

        // Hàng 1: Player 1 (Trái) - Round & Timer (Giữa) - Player 2 (Phải)
        JPanel headerRow = new JPanel(new BorderLayout(10, 0));
        headerRow.setOpaque(false);

        // Player 1 (Bạn)
        JPanel p1Box = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p1Box.setOpaque(false);
        lblPlayer1Badge = BrutalUI.createBadge("BẠN", new Color(254, 243, 199), new Color(146, 64, 14));
        lblPlayer1Badge.setPreferredSize(new Dimension(55, 32));
        
        JPanel p1Info = new JPanel(new GridLayout(2, 1, 0, 2));
        p1Info.setOpaque(false);
        lblPlayer1Name = new JLabel("Bạn");
        lblPlayer1Name.setFont(BrutalUI.FONT_SMALL_BOLD);
        lblPlayer1Name.setForeground(BrutalUI.COLOR_MUTED);
        
        lblPlayer1Score = new JLabel("0 điểm");
        lblPlayer1Score.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlayer1Score.setForeground(BrutalUI.COLOR_BLACK);
        p1Info.add(lblPlayer1Name);
        p1Info.add(lblPlayer1Score);

        p1Box.add(lblPlayer1Badge);
        p1Box.add(p1Info);
        headerRow.add(p1Box, BorderLayout.WEST);

        // Center: Round Pill Badge & Countdown Text
        JPanel centerInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        centerInfo.setOpaque(false);
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeWrapper.setOpaque(false);
        lblRoundBadge = BrutalUI.createBadge("CÂU HỎI 1 / 5", new Color(244, 244, 245), BrutalUI.COLOR_BLACK);
        badgeWrapper.add(lblRoundBadge);


        lblTimerText = new JLabel("10s", SwingConstants.CENTER);
        lblTimerText.setFont(BrutalUI.FONT_TIMER);
        lblTimerText.setForeground(BrutalUI.COLOR_BLACK);

        centerInfo.add(badgeWrapper);
        centerInfo.add(lblTimerText);
        headerRow.add(centerInfo, BorderLayout.CENTER);

        // Player 2 (Đối thủ)
        JPanel p2Box = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p2Box.setOpaque(false);
        
        JPanel p2Info = new JPanel(new GridLayout(2, 1, 0, 2));
        p2Info.setOpaque(false);
        lblPlayer2Name = new JLabel("Đối thủ", SwingConstants.RIGHT);
        lblPlayer2Name.setFont(BrutalUI.FONT_SMALL_BOLD);
        lblPlayer2Name.setForeground(BrutalUI.COLOR_MUTED);
        
        lblPlayer2Score = new JLabel("0 điểm", SwingConstants.RIGHT);
        lblPlayer2Score.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlayer2Score.setForeground(BrutalUI.COLOR_BLACK);
        p2Info.add(lblPlayer2Name);
        p2Info.add(lblPlayer2Score);

        lblPlayer2Badge = BrutalUI.createBadge("ĐỐI", new Color(239, 246, 255), new Color(29, 78, 216));
        lblPlayer2Badge.setPreferredSize(new Dimension(55, 32));

        p2Box.add(p2Info);
        p2Box.add(lblPlayer2Badge);
        headerRow.add(p2Box, BorderLayout.EAST);

        topCard.add(headerRow, BorderLayout.CENTER);

        // Hàng 2: Thanh đếm ngược thời gian phẳng Neo-Brutalist
        timerProgressBar = new BrutalUI.BrutalProgressBar(0, 10);
        timerProgressBar.setValue(10);
        topCard.add(timerProgressBar, BorderLayout.SOUTH);

        add(topCard, BorderLayout.NORTH);

        // =========================================================================
        // 2. CENTER PANEL: Question Card & 4 Brutal Answer Buttons (Yêu cầu 5, 7)
        // =========================================================================
        JPanel centerContainer = new JPanel(new BorderLayout(14, 14));
        centerContainer.setOpaque(false);

        // Thẻ Câu hỏi Card viền đen bóng cứng
        BrutalUI.CardPanel questionCard = new BrutalUI.CardPanel(16, 4);
        questionCard.setLayout(new BorderLayout(10, 10));

        JPanel topicHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        topicHeader.setOpaque(false);
        lblTopicBadge = BrutalUI.createBadge("Chủ đề: Đa Lĩnh Vực · Độ khó: Trung Bình", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);
        topicHeader.add(lblTopicBadge);
        questionCard.add(topicHeader, BorderLayout.NORTH);

        txtQuestion = new JTextArea("Đang tải nội dung câu hỏi...");
        txtQuestion.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtQuestion.setForeground(BrutalUI.COLOR_BLACK);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setLineWrap(true);
        txtQuestion.setEditable(false);
        txtQuestion.setOpaque(false);
        txtQuestion.setFocusable(false);
        txtQuestion.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtQuestion.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        questionCard.add(txtQuestion, BorderLayout.CENTER);

        lblQuestionImage = new JLabel("", SwingConstants.CENTER);
        lblQuestionImage.setPreferredSize(new Dimension(280, 120));
        lblQuestionImage.setVisible(false);
        questionCard.add(lblQuestionImage, BorderLayout.SOUTH);

        centerContainer.add(questionCard, BorderLayout.NORTH);

        // Lưới 4 Đáp Án A, B, C, D (2x2 Grid)
        JPanel answersGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        answersGrid.setOpaque(false);

        btnAnswerA = new BrutalUI.AnswerButton("A", "Đang tải...");
        btnAnswerB = new BrutalUI.AnswerButton("B", "Đang tải...");
        btnAnswerC = new BrutalUI.AnswerButton("C", "Đang tải...");
        btnAnswerD = new BrutalUI.AnswerButton("D", "Đang tải...");

        btnAnswerA.addActionListener(e -> handleAnswerClick(btnAnswerA));
        btnAnswerB.addActionListener(e -> handleAnswerClick(btnAnswerB));
        btnAnswerC.addActionListener(e -> handleAnswerClick(btnAnswerC));
        btnAnswerD.addActionListener(e -> handleAnswerClick(btnAnswerD));

        answersGrid.add(btnAnswerA);
        answersGrid.add(btnAnswerB);
        answersGrid.add(btnAnswerC);
        answersGrid.add(btnAnswerD);
        centerContainer.add(answersGrid, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        // =========================================================================
        // 3. BOTTOM PANEL: Realtime Status & Surrender Button (Yêu cầu 6)
        // =========================================================================
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel statusBox = new JPanel(new GridLayout(2, 1, 0, 4));
        statusBox.setOpaque(false);

        lblStatus = new JLabel("⚡ Hãy chọn 1 đáp án nhanh nhất để nhận thưởng thời gian!");
        lblStatus.setFont(BrutalUI.FONT_BODY_BOLD);
        lblStatus.setForeground(BrutalUI.COLOR_BLACK);

        lblOpponentStatus = new JLabel("Đối thủ đang suy nghĩ...");
        lblOpponentStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblOpponentStatus.setForeground(BrutalUI.COLOR_MUTED);

        statusBox.add(lblStatus);
        statusBox.add(lblOpponentStatus);
        bottomPanel.add(statusBox, BorderLayout.CENTER);

        btnSurrender = new BrutalUI.BrutalButton("🏳️ Đầu Hàng", BrutalUI.BrutalButton.Style.DANGER, 12);
        btnSurrender.setPreferredSize(new Dimension(130, 42));
        btnSurrender.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn đầu hàng và xử thua trận đấu này?",
                    "Xác nhận đầu hàng",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (questionTimer != null) questionTimer.stop();
                battleService.surrender(currentMatchId);
                JOptionPane.showMessageDialog(this, "Bạn đã đầu hàng và bị xử thua trận đấu này!", "Đầu Hàng", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
            }
        });
        bottomPanel.add(btnSurrender, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Bắt đầu 1 câu hỏi mới nhận từ Server (Yêu cầu 5, 7)
     */
    public void renderNewQuestion(BattleDTO.BattleQuestionNotify question) {
        this.currentMatchId = question.matchId;
        this.currentQuestionIndex = question.questionIndex;
        this.currentOptions = question.options;
        this.answerSubmitted = false;
        this.selectedAnswerId = null;

        if (question.questionIndex == 1) {
            lblPlayer1Score.setText("0 điểm");
            lblPlayer2Score.setText("0 điểm");
        }

        lblRoundBadge.setText("CÂU HỎI " + question.questionIndex + " / " + question.totalQuestions);
        txtQuestion.setText(question.questionContent);

        // Hiển thị badge phong cách Neo-Brutalist kết hợp Chủ đề & Độ khó (Yêu cầu 4)
        String topicStr = (question.topicName != null && !question.topicName.trim().isEmpty())
                ? question.topicName : "Đa lĩnh vực";
        String diffStr = formatDifficultyDisplay(question.difficulty);
        lblTopicBadge.setText("⚡ " + topicStr.toUpperCase() + " · " + diffStr.toUpperCase() + " · THỜI GIAN THỰC (10S/CÂU)");

        if (question.questionImage != null && !question.questionImage.isEmpty()) {
            if (com.quizzlive.shared.util.ImageBase64Util.isBase64Image(question.questionImage)) {
                java.awt.image.BufferedImage b64Img = com.quizzlive.shared.util.ImageBase64Util.base64ToImage(question.questionImage);
                if (b64Img != null) {
                    Image scaled = b64Img.getScaledInstance(280, 120, Image.SCALE_SMOOTH);
                    lblQuestionImage.setIcon(new ImageIcon(scaled));
                    lblQuestionImage.setText("");
                } else {
                    lblQuestionImage.setText("[ Ảnh minh họa ]");
                }
            } else {
                lblQuestionImage.setIcon(null);
                lblQuestionImage.setText("[ Ảnh minh họa: " + question.questionImage + " ]");
            }
            lblQuestionImage.setVisible(true);
        } else {
            lblQuestionImage.setIcon(null);
            lblQuestionImage.setText("");
            lblQuestionImage.setVisible(false);
        }

        resetAnswerButtonStyles();

        // Gán nội dung 4 đáp án
        if (question.options != null && question.options.size() >= 4) {
            btnAnswerA.setAnswerContent(question.options.get(0).content);
            btnAnswerB.setAnswerContent(question.options.get(1).content);
            btnAnswerC.setAnswerContent(question.options.get(2).content);
            btnAnswerD.setAnswerContent(question.options.get(3).content);
        }

        lblStatus.setText("⚡ Hãy chọn 1 đáp án nhanh nhất để nhận tối đa 200 điểm!");
        lblStatus.setForeground(BrutalUI.COLOR_BLACK);
        lblOpponentStatus.setText("Đối thủ đang suy nghĩ...");

        // Khởi động đếm ngược 10 giây
        startQuestionTimer(question.timeLimitSeconds > 0 ? question.timeLimitSeconds : 10);
    }

    private void startQuestionTimer(int seconds) {
        if (questionTimer != null) questionTimer.stop();
        this.timeRemaining = seconds;
        timerProgressBar.setMaximum(seconds);
        timerProgressBar.setValue(seconds);
        lblTimerText.setText(seconds + "s");

        questionTimer = new Timer(1000, e -> {
            timeRemaining--;
            lblTimerText.setText((timeRemaining < 10 ? "0" : "") + timeRemaining + "s");
            timerProgressBar.setValue(timeRemaining);

            if (timeRemaining <= 0) {
                questionTimer.stop();
                if (!answerSubmitted) {
                    answerSubmitted = true;
                    battleService.submitAnswer(currentMatchId, currentQuestionIndex, null, 10);
                    lblStatus.setText("⏰ Hết thời gian 10 giây!");
                    lblStatus.setForeground(BrutalUI.COLOR_WRONG_BORDER);
                    disableAnswerButtons();
                }
            }
        });
        questionTimer.start();
    }

    private void handleAnswerClick(BrutalUI.AnswerButton clickedBtn) {
        if (answerSubmitted) return;
        answerSubmitted = true;

        if (questionTimer != null) questionTimer.stop();

        // Đổi trạng thái nút người dùng chọn (màu vàng chờ kết quả)
        clickedBtn.setState(BrutalUI.AnswerButton.State.SELECTED);

        int responseTime = 10 - timeRemaining;
        Long ansId = getAnswerIdFromButton(clickedBtn);
        this.selectedAnswerId = ansId;

        // Gửi đáp án lên server
        battleService.submitAnswer(currentMatchId, currentQuestionIndex, ansId, responseTime);

        lblStatus.setText("✓ Đã nộp đáp án (" + responseTime + "s). Đang đợi kết quả từ đối thủ...");
        lblStatus.setForeground(new Color(146, 64, 14));
        disableAnswerButtons();
    }

    /**
     * Hiển thị lựa chọn của đối thủ và đáp án đúng khi hết vòng (Yêu cầu 6)
     */
    public void renderRoundResult(BattleDTO.RoundResultNotify result) {
        if (questionTimer != null) questionTimer.stop();

        Long myUserId = mainFrame.getCurrentUserId();
        boolean isMePlayer1 = (myUserId != null && result.player1 != null && myUserId.equals(result.player1.userId));
        BattleDTO.PlayerRoundInfo me = isMePlayer1 ? result.player1 : result.player2;
        BattleDTO.PlayerRoundInfo opponent = isMePlayer1 ? result.player2 : result.player1;

        // 1. Cập nhật tên và điểm số chính xác của từng người
        if (me != null) {
            lblPlayer1Name.setText(me.username != null ? me.username + " (Bạn)" : "Bạn");
            lblPlayer1Score.setText(me.totalScore + " điểm");
        }
        if (opponent != null) {
            lblPlayer2Name.setText(opponent.username != null ? opponent.username + " (Đối thủ)" : "Đối thủ");
            lblPlayer2Score.setText(opponent.totalScore + " điểm");
        }

        // 2. Tô xanh đáp án đúng
        highlightCorrectAnswer(result.correctAnswerId);

        // 3. Nếu người chơi chọn sai -> Tô đỏ nút người chơi chọn
        if (selectedAnswerId != null && selectedAnswerId != result.correctAnswerId) {
            highlightWrongAnswer(selectedAnswerId);
        }

        // 4. Hiển thị lựa chọn của đối thủ (Yêu cầu 6)
        if (opponent != null) {
            String oppText = opponent.selectedAnswerId != null ?
                    "Đối thủ chọn: " + getAnswerKey(opponent.selectedAnswerId) + (opponent.isCorrect ? " (Đúng ✅)" : " (Sai ❌)")
                    : "Đối thủ không kịp trả lời!";
            lblOpponentStatus.setText(oppText);
        }
        lblStatus.setText("Đáp án đúng là: " + getAnswerKey(result.correctAnswerId) + " | Chuẩn bị sang câu tiếp theo...");
        lblStatus.setForeground(BrutalUI.COLOR_CORRECT_BORDER);
    }

    /**
     * Hiển thị kết quả chung cuộc của trận đấu (Yêu cầu 8)
     */
    public void renderFinalResult(BattleDTO.FinalResultNotify result) {
        if (questionTimer != null) questionTimer.stop();

        Long myUserId = mainFrame.getCurrentUserId();
        boolean isMePlayer1 = (myUserId != null && result.player1 != null && myUserId.equals(result.player1.userId));
        BattleDTO.PlayerRoundInfo me = isMePlayer1 ? result.player1 : result.player2;
        BattleDTO.PlayerRoundInfo opponent = isMePlayer1 ? result.player2 : result.player1;

        int myScore = (me != null) ? me.totalScore : 0;
        int oppScore = (opponent != null) ? opponent.totalScore : 0;

        String title;
        String message;
        int messageType;

        if (result.isDraw) {
            title = "KẾT QUẢ: HÒA!";
            message = "Trận đấu kết thúc với tỉ số hòa!\n\nĐiểm của bạn: " + myScore + "\nĐiểm đối thủ: " + oppScore;
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else if (result.winnerUserId != null && myUserId != null && result.winnerUserId.equals(myUserId)) {
            title = "🏆 CHIẾN THẮNG!";
            message = "Chúc mừng! Bạn đã giành chiến thắng xuất sắc!\n\nĐiểm của bạn: " + myScore + "\nĐiểm đối thủ: " + oppScore;
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else {
            title = "💔 THẤT BẠI!";
            message = "Bạn đã thua trận đấu này!\n\nĐiểm của bạn: " + myScore + "\nĐiểm đối thủ: " + oppScore;
            messageType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(this, message, title, messageType);
        // Quay về phòng chờ
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    private void resetAnswerButtonStyles() {
        BrutalUI.AnswerButton[] buttons = {btnAnswerA, btnAnswerB, btnAnswerC, btnAnswerD};
        for (BrutalUI.AnswerButton b : buttons) {
            b.setEnabled(true);
            b.setState(BrutalUI.AnswerButton.State.NORMAL);
        }
    }

    private void disableAnswerButtons() {
        btnAnswerA.setEnabled(false);
        btnAnswerB.setEnabled(false);
        btnAnswerC.setEnabled(false);
        btnAnswerD.setEnabled(false);
    }

    private Long getAnswerIdFromButton(BrutalUI.AnswerButton btn) {
        if (currentOptions == null) return null;
        if (btn == btnAnswerA && currentOptions.size() > 0) return currentOptions.get(0).answerId;
        if (btn == btnAnswerB && currentOptions.size() > 1) return currentOptions.get(1).answerId;
        if (btn == btnAnswerC && currentOptions.size() > 2) return currentOptions.get(2).answerId;
        if (btn == btnAnswerD && currentOptions.size() > 3) return currentOptions.get(3).answerId;
        return null;
    }

    private String getAnswerKey(long answerId) {
        if (currentOptions != null) {
            for (BattleDTO.QuestionAnswerOption opt : currentOptions) {
                if (opt.answerId == answerId) return opt.key;
            }
        }
        return "ID=" + answerId;
    }

    private void highlightCorrectAnswer(long correctAnswerId) {
        BrutalUI.AnswerButton btn = findButtonByAnswerId(correctAnswerId);
        if (btn != null) {
            btn.setState(BrutalUI.AnswerButton.State.CORRECT);
        }
    }

    private void highlightWrongAnswer(long wrongAnswerId) {
        BrutalUI.AnswerButton btn = findButtonByAnswerId(wrongAnswerId);
        if (btn != null) {
            btn.setState(BrutalUI.AnswerButton.State.WRONG);
        }
    }

    private BrutalUI.AnswerButton findButtonByAnswerId(long answerId) {
        if (currentOptions == null) return null;
        if (currentOptions.size() > 0 && currentOptions.get(0).answerId == answerId) return btnAnswerA;
        if (currentOptions.size() > 1 && currentOptions.get(1).answerId == answerId) return btnAnswerB;
        if (currentOptions.size() > 2 && currentOptions.get(2).answerId == answerId) return btnAnswerC;
        if (currentOptions.size() > 3 && currentOptions.get(3).answerId == answerId) return btnAnswerD;
        return null;
    }

    private String formatDifficultyDisplay(String diff) {
        if (diff == null) return "Trung bình (MEDIUM)";
        String u = diff.toUpperCase();
        if (u.contains("EASY") || u.contains("DỄ")) return "Dễ (EASY)";
        if (u.contains("HARD") || u.contains("KHÓ")) return "Khó (HARD)";
        if (u.contains("MEDIUM") || u.contains("TRUNG BÌNH")) return "Trung bình (MEDIUM)";
        return diff;
    }
}
