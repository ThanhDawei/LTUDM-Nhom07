package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientBattleService;
import com.quizzlive.shared.dto.BattleDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Giao diện Trận đấu Realtime 1v1
 * Phụ trách:
 * - Yêu cầu 5: Mỗi trận 5 câu, mỗi câu 4 đáp án, đếm ngược 10 giây, tính điểm theo thời gian
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

    // UI Header
    private JLabel lblRoundIndicator;
    private JLabel lblPlayer1Score;
    private JLabel lblPlayer2Score;
    private JProgressBar timerProgressBar;
    private JLabel lblTimerText;

    // UI Question & Image
    private JTextArea txtQuestion;
    private JLabel lblQuestionImage;

    // UI Answers (4 đáp án)
    private JButton btnAnswerA;
    private JButton btnAnswerB;
    private JButton btnAnswerC;
    private JButton btnAnswerD;
    private List<BattleDTO.QuestionAnswerOption> currentOptions;

    // UI Status / Opponent feedback
    private JLabel lblStatus;
    private JLabel lblOpponentStatus;
    private JButton btnSurrender;

    public BattlePanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // 1. TOP BAR: Match Header, Scores, Countdown 10s (Yêu cầu 5)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel scoreBox = new JPanel(new GridLayout(1, 3, 10, 0));
        scoreBox.setOpaque(false);
        lblPlayer1Score = new JLabel("Bạn: 0 điểm", SwingConstants.LEFT);
        lblPlayer1Score.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPlayer1Score.setForeground(new Color(41, 128, 185));

        lblRoundIndicator = new JLabel("CÂU 1 / 5", SwingConstants.CENTER);
        lblRoundIndicator.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRoundIndicator.setForeground(new Color(52, 73, 94));

        lblPlayer2Score = new JLabel("Đối thủ: 0 điểm", SwingConstants.RIGHT);
        lblPlayer2Score.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPlayer2Score.setForeground(new Color(231, 76, 60));

        scoreBox.add(lblPlayer1Score);
        scoreBox.add(lblRoundIndicator);
        scoreBox.add(lblPlayer2Score);
        topPanel.add(scoreBox, BorderLayout.NORTH);

        // Thanh đếm ngược 10 giây
        JPanel timerBox = new JPanel(new BorderLayout(10, 0));
        timerBox.setOpaque(false);
        lblTimerText = new JLabel("10s", SwingConstants.CENTER);
        lblTimerText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTimerText.setForeground(new Color(230, 126, 34));
        lblTimerText.setPreferredSize(new Dimension(40, 20));

        timerProgressBar = new JProgressBar(0, 10);
        timerProgressBar.setValue(10);
        timerProgressBar.setForeground(new Color(230, 126, 34));
        timerProgressBar.setPreferredSize(new Dimension(300, 12));

        timerBox.add(lblTimerText, BorderLayout.WEST);
        timerBox.add(timerProgressBar, BorderLayout.CENTER);
        topPanel.add(timerBox, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 2. CENTER PANEL: Question Text, Image & 4 Answers (Yêu cầu 5, 7)
        JPanel centerCard = new JPanel(new BorderLayout(10, 10));
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel questionBox = new JPanel(new BorderLayout(8, 8));
        questionBox.setOpaque(false);

        txtQuestion = new JTextArea("Nội dung câu hỏi đang được tải...");
        txtQuestion.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setLineWrap(true);
        txtQuestion.setEditable(false);
        txtQuestion.setOpaque(false);
        txtQuestion.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        questionBox.add(txtQuestion, BorderLayout.NORTH);

        lblQuestionImage = new JLabel("", SwingConstants.CENTER);
        lblQuestionImage.setPreferredSize(new Dimension(250, 120));
        lblQuestionImage.setVisible(false);
        questionBox.add(lblQuestionImage, BorderLayout.CENTER);

        centerCard.add(questionBox, BorderLayout.NORTH);

        // 4 nút đáp án A, B, C, D
        JPanel answersGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        answersGrid.setOpaque(false);

        btnAnswerA = createAnswerButton("A");
        btnAnswerB = createAnswerButton("B");
        btnAnswerC = createAnswerButton("C");
        btnAnswerD = createAnswerButton("D");

        answersGrid.add(btnAnswerA);
        answersGrid.add(btnAnswerB);
        answersGrid.add(btnAnswerC);
        answersGrid.add(btnAnswerD);
        centerCard.add(answersGrid, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);

        // 3. BOTTOM STATUS PANEL: Opponent selection indicator & Surrender button (Yêu cầu 6)
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel statusBox = new JPanel(new GridLayout(2, 1));
        statusBox.setOpaque(false);
        lblStatus = new JLabel("Hãy chọn 1 đáp án nhanh nhất có thể để nhận điểm tối đa!");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblOpponentStatus = new JLabel("Đối thủ đang suy nghĩ...");
        lblOpponentStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblOpponentStatus.setForeground(Color.GRAY);

        statusBox.add(lblStatus);
        statusBox.add(lblOpponentStatus);
        bottomPanel.add(statusBox, BorderLayout.CENTER);

        btnSurrender = new JButton("Đầu Hàng");
        btnSurrender.setBackground(new Color(231, 76, 60));
        btnSurrender.setForeground(Color.WHITE);
        btnSurrender.setFocusPainted(false);
        btnSurrender.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đầu hàng trận đấu này?", "Xác nhận đầu hàng", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                battleService.surrender(currentMatchId);
            }
        });
        bottomPanel.add(btnSurrender, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createAnswerButton(String prefix) {
        JButton btn = new JButton(prefix + ". Đang tải...");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(new Color(240, 243, 246));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> handleAnswerClick(btn));
        return btn;
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

        lblRoundIndicator.setText("CÂU " + question.questionIndex + " / " + question.totalQuestions);
        txtQuestion.setText(question.questionContent);

        if (question.questionImage != null && !question.questionImage.isEmpty()) {
            lblQuestionImage.setText("[ Ảnh: " + question.questionImage + " ]");
            lblQuestionImage.setVisible(true);
        } else {
            lblQuestionImage.setVisible(false);
        }

        resetAnswerButtonStyles();

        // Gán nội dung 4 đáp án
        if (question.options != null && question.options.size() >= 4) {
            btnAnswerA.setText("A. " + question.options.get(0).content);
            btnAnswerB.setText("B. " + question.options.get(1).content);
            btnAnswerC.setText("C. " + question.options.get(2).content);
            btnAnswerD.setText("D. " + question.options.get(3).content);
        }

        lblStatus.setText("Hãy chọn 1 đáp án nhanh nhất có thể!");
        lblStatus.setForeground(Color.BLACK);
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
            lblTimerText.setText(timeRemaining + "s");
            timerProgressBar.setValue(timeRemaining);

            if (timeRemaining <= 0) {
                questionTimer.stop();
                if (!answerSubmitted) {
                    // Hết 10s tự động nộp đáp án null (0 điểm)
                    answerSubmitted = true;
                    battleService.submitAnswer(currentMatchId, currentQuestionIndex, null, 10);
                    lblStatus.setText("Hết thời gian 10 giây!");
                    lblStatus.setForeground(Color.RED);
                    disableAnswerButtons();
                }
            }
        });
        questionTimer.start();
    }

    private void handleAnswerClick(JButton clickedBtn) {
        if (answerSubmitted) return;
        answerSubmitted = true;

        if (questionTimer != null) questionTimer.stop();

        // Đổi màu nút người dùng chọn (màu vàng chờ kết quả)
        clickedBtn.setBackground(new Color(254, 211, 48));

        int responseTime = 10 - timeRemaining;
        Long ansId = getAnswerIdFromButton(clickedBtn);
        this.selectedAnswerId = ansId;

        // Gửi đáp án lên server
        battleService.submitAnswer(currentMatchId, currentQuestionIndex, ansId, responseTime);

        lblStatus.setText("Đã nộp đáp án (" + responseTime + "s). Đang đợi đối thủ...");
        disableAnswerButtons();
    }

    /**
     * Hiển thị lựa chọn của đối thủ và đáp án đúng khi hết vòng (Yêu cầu 6)
     */
    public void renderRoundResult(BattleDTO.RoundResultNotify result) {
        if (questionTimer != null) questionTimer.stop();

        // 1. Cập nhật điểm số
        lblPlayer1Score.setText("Bạn: " + result.player1.totalScore + " điểm");
        lblPlayer2Score.setText("Đối thủ: " + result.player2.totalScore + " điểm");

        // 2. Tô xanh đáp án đúng
        highlightCorrectAnswer(result.correctAnswerId);

        // 3. Nếu người chơi chọn sai -> Tô đỏ nút người chơi chọn
        if (selectedAnswerId != null && selectedAnswerId != result.correctAnswerId) {
            highlightWrongAnswer(selectedAnswerId);
        }

        // 4. Hiển thị lựa chọn của đối thủ (Yêu cầu 6)
        String oppText = result.player2.selectedAnswerId != null ?
                "Đối thủ chọn đáp án: " + getAnswerKey(result.player2.selectedAnswerId) + (result.player2.isCorrect ? " (Đúng ✅)" : " (Sai ❌)")
                : "Đối thủ không kịp trả lời!";
        lblOpponentStatus.setText(oppText);
        lblStatus.setText("Đáp án đúng là: " + getAnswerKey(result.correctAnswerId) + " | Chuẩn bị sang câu tiếp theo...");
    }

    /**
     * Hiển thị kết quả chung cuộc của trận đấu (Yêu cầu 8)
     */
    public void renderFinalResult(BattleDTO.FinalResultNotify result) {
        if (questionTimer != null) questionTimer.stop();

        String title;
        String message;
        if (result.isDraw) {
            title = "KẾT QUẢ: HÒA!";
            message = "Trận đấu kết thúc với tỉ số hòa!\nĐiểm của bạn: " + result.player1.totalScore + "\nĐiểm đối thủ: " + result.player2.totalScore;
        } else if (result.winnerUserId != null && result.winnerUserId == result.player1.userId) {
            title = "🏆 CHIẾN THẮNG!";
            message = "Chúc mừng! Bạn đã giành chiến thắng!\nĐiểm của bạn: " + result.player1.totalScore + "\nĐiểm đối thủ: " + result.player2.totalScore;
        } else {
            title = "💔 THẤT BẠI!";
            message = "Bạn đã thua trận đấu này!\nĐiểm của bạn: " + result.player1.totalScore + "\nĐiểm đối thủ: " + result.player2.totalScore;
        }

        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        // Quay về phòng chờ
        mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
    }

    private void resetAnswerButtonStyles() {
        JButton[] buttons = {btnAnswerA, btnAnswerB, btnAnswerC, btnAnswerD};
        for (JButton b : buttons) {
            b.setEnabled(true);
            b.setBackground(new Color(240, 243, 246));
            b.setForeground(Color.BLACK);
        }
    }

    private void disableAnswerButtons() {
        btnAnswerA.setEnabled(false);
        btnAnswerB.setEnabled(false);
        btnAnswerC.setEnabled(false);
        btnAnswerD.setEnabled(false);
    }

    private Long getAnswerIdFromButton(JButton btn) {
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
        JButton btn = findButtonByAnswerId(correctAnswerId);
        if (btn != null) {
            btn.setBackground(new Color(46, 204, 113)); // Xanh lá
            btn.setForeground(Color.WHITE);
        }
    }

    private void highlightWrongAnswer(long wrongAnswerId) {
        JButton btn = findButtonByAnswerId(wrongAnswerId);
        if (btn != null) {
            btn.setBackground(new Color(231, 76, 60)); // Đỏ
            btn.setForeground(Color.WHITE);
        }
    }

    private JButton findButtonByAnswerId(long answerId) {
        if (currentOptions == null) return null;
        if (currentOptions.size() > 0 && currentOptions.get(0).answerId == answerId) return btnAnswerA;
        if (currentOptions.size() > 1 && currentOptions.get(1).answerId == answerId) return btnAnswerB;
        if (currentOptions.size() > 2 && currentOptions.get(2).answerId == answerId) return btnAnswerC;
        if (currentOptions.size() > 3 && currentOptions.get(3).answerId == answerId) return btnAnswerD;
        return null;
    }
}
