package com.quizzlive.server.service;

import com.quizzlive.server.dao.MatchConfigDAO;
import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.Answer;
import com.quizzlive.server.entity.MatchConfig;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BattleRoom {
    private final long matchId;
    private final ClientSession player1;
    private final ClientSession player2;
    private final Integer topicId;
    private final String difficulty;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MatchConfigDAO matchConfigDAO = new MatchConfigDAO();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // cấu hình trận đấu (nạp động từ CSDL do Admin quản trị)
    private int totalQuestions = 5;
    private int timeLimitSeconds = 10;
    private int baseScore = 100;
    private boolean speedBonus = true;

    private List<Question> questions = new ArrayList<>();// list câu hỏi ko trùng nhau
    private int currentQuestionIndex = 0;
    private ScheduledFuture<?> currentRoundTimer = null;

    // trạng thái câu hỏi
    private Long p1SelectedAnswerId = null;
    private int p1ResponseTime = 0;
    private boolean p1Answered = false;

    private Long p2SelectedAnswerId = null;
    private int p2ResponseTime = 0;
    private boolean p2Answered = false;

    // điểm
    private int p1TotalScore = 0;
    private int p2TotalScore = 0;
    private boolean matchFinished = false;

    public BattleRoom(long matchId, ClientSession player1, ClientSession player2) {
        this(matchId, player1, player2, null, "MEDIUM");
    }

    public BattleRoom(long matchId, ClientSession player1, ClientSession player2, Integer topicId, String difficulty) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
        this.topicId = topicId;
        this.difficulty = difficulty;
        loadMatchConfig();
    }

    private void loadMatchConfig() {
        MatchConfig config = matchConfigDAO.getActiveConfig();
        if (config != null) {
            if (config.getQuestionCount() > 0) this.totalQuestions = config.getQuestionCount();
            if (config.getTimePerQuestion() > 0) this.timeLimitSeconds = config.getTimePerQuestion();
            if (config.getBaseScore() > 0) this.baseScore = config.getBaseScore();
            this.speedBonus = config.isSpeedBonus();
        }
    }

    public void start() {
        questions = questionDAO.getRandomQuestions(topicId, difficulty, totalQuestions);// lấy câu hỏi ngẫu nhiên

        if (questions == null || questions.size() < totalQuestions) {// nếu không đủ câu hỏi thì lấy câu hỏi ngẫu nhiên
            questions = questionDAO.getRandomQuestions(null, null, totalQuestions);
        }

        Packet startPacket = new Packet(OpCode.BATTLE_START_NOTIFY, "{\"matchId\":" + matchId + "}");// gửi thông báo
                                                                                                     // bắt đầu trận đấu
        player1.sendPacket(startPacket);
        player2.sendPacket(startPacket);

        scheduler.schedule(this::nextQuestion, 2000, TimeUnit.MILLISECONDS);
    }

    public synchronized void nextQuestion() {
        if (currentQuestionIndex >= totalQuestions) {
            finishMatch();
            return;
        }

        // reset trạng thái ng chơi
        p1SelectedAnswerId = null;
        p1ResponseTime = 0;
        p1Answered = false;

        p2SelectedAnswerId = null;
        p2ResponseTime = 0;
        p2Answered = false;

        Question currentQ = questions.get(currentQuestionIndex);

        List<Answer> answers = new ArrayList<>(currentQ.getAnswers());
        Collections.shuffle(answers);// xáo

        BattleDTO.BattleQuestionNotify questionNotify = new BattleDTO.BattleQuestionNotify();
        questionNotify.matchId = matchId;
        questionNotify.questionIndex = currentQuestionIndex + 1;
        questionNotify.totalQuestions = totalQuestions;
        questionNotify.timeLimitSeconds = timeLimitSeconds;
        questionNotify.questionContent = currentQ.getContent();
        questionNotify.questionImage = currentQ.getImage();
        questionNotify.options = new ArrayList<>();

        String[] keys = { "A", "B", "C", "D" };
        for (int i = 0; i < answers.size() && i < 4; i++) {
            Answer ans = answers.get(i);
            BattleDTO.QuestionAnswerOption opt = new BattleDTO.QuestionAnswerOption();
            opt.answerId = ans.getAnswerId();
            opt.key = keys[i];
            opt.content = ans.getContent();
            opt.image = ans.getImage();
            questionNotify.options.add(opt);
        }
        // gửi câu hỏi
        Packet questionPacket = new Packet(OpCode.BATTLE_QUESTION_NOTIFY, JsonUtil.toJson(questionNotify));
        player1.sendPacket(questionPacket);
        player2.sendPacket(questionPacket);

        currentRoundTimer = scheduler.schedule(this::onRoundTimeout, timeLimitSeconds, TimeUnit.SECONDS);
    }

    // nhận câu hỏi tính điểm và dồng bộ
    public synchronized void submitAnswer(long userId, Long selectedAnswerId, int responseTime) {
        if (userId == player1.getUserId() && !p1Answered) {
            p1Answered = true;
            p1SelectedAnswerId = selectedAnswerId;
            p1ResponseTime = Math.min(responseTime, timeLimitSeconds);
        } else if (userId == player2.getUserId() && !p2Answered) {
            p2Answered = true;
            p2SelectedAnswerId = selectedAnswerId;
            p2ResponseTime = Math.min(responseTime, timeLimitSeconds);
        }

        if (p1Answered && p2Answered) {
            if (currentRoundTimer != null && !currentRoundTimer.isDone()) {
                currentRoundTimer.cancel(false);
            }
            onBothAnswered();
        }
    }

    // quá 10s mà có 1 ng ko trl
    private synchronized void onRoundTimeout() {
        if (!p1Answered) {
            p1Answered = true;
            p1SelectedAnswerId = null;
            p1ResponseTime = timeLimitSeconds;
        }
        if (!p2Answered) {
            p2Answered = true;
            p2SelectedAnswerId = null;
            p2ResponseTime = timeLimitSeconds;
        }

        onBothAnswered();
    }

    private synchronized void onBothAnswered() {
        Question currentQ = questions.get(currentQuestionIndex);

        long correctAnswerId = 0;
        for (Answer ans : currentQ.getAnswers()) {
            if (ans.isCorrect()) {
                correctAnswerId = ans.getAnswerId();
                break;
            }
        }

        int p1ScoreEarned = 0;
        boolean p1Correct = (p1SelectedAnswerId != null && p1SelectedAnswerId == correctAnswerId);
        if (p1Correct) {
            int remainingTime = Math.max(0, timeLimitSeconds - p1ResponseTime);
            p1ScoreEarned = baseScore + (speedBonus ? (remainingTime * 10) : 0);
        }
        p1TotalScore += p1ScoreEarned;

        int p2ScoreEarned = 0;
        boolean p2Correct = (p2SelectedAnswerId != null && p2SelectedAnswerId == correctAnswerId);
        if (p2Correct) {
            int remainingTime = Math.max(0, timeLimitSeconds - p2ResponseTime);
            p2ScoreEarned = baseScore + (speedBonus ? (remainingTime * 10) : 0);
        }
        p2TotalScore += p2ScoreEarned;

        BattleDTO.RoundResultNotify roundResult = new BattleDTO.RoundResultNotify();
        roundResult.matchId = matchId;
        roundResult.questionIndex = currentQuestionIndex + 1;
        roundResult.correctAnswerId = correctAnswerId;
        roundResult.explanation = currentQ.getExplanation();

        roundResult.player1 = new BattleDTO.PlayerRoundInfo();
        roundResult.player1.userId = player1.getUserId();
        roundResult.player1.username = player1.getUsername();
        roundResult.player1.selectedAnswerId = p1SelectedAnswerId;
        roundResult.player1.isCorrect = p1Correct;
        roundResult.player1.scoreEarned = p1ScoreEarned;
        roundResult.player1.totalScore = p1TotalScore;

        roundResult.player2 = new BattleDTO.PlayerRoundInfo();
        roundResult.player2.userId = player2.getUserId();
        roundResult.player2.username = player2.getUsername();
        roundResult.player2.selectedAnswerId = p2SelectedAnswerId;
        roundResult.player2.isCorrect = p2Correct;
        roundResult.player2.scoreEarned = p2ScoreEarned;
        roundResult.player2.totalScore = p2TotalScore;

        Packet roundResultPacket = new Packet(OpCode.BATTLE_ROUND_RESULT_NOTIFY, JsonUtil.toJson(roundResult));
        player1.sendPacket(roundResultPacket);
        player2.sendPacket(roundResultPacket);

        currentQuestionIndex++;

        scheduler.schedule(this::nextQuestion, 2500, TimeUnit.MILLISECONDS);
    }

    private synchronized void finishMatch() {
        if (matchFinished)
            return;
        matchFinished = true;

        boolean isDraw = (p1TotalScore == p2TotalScore);
        Long winnerUserId = null;
        String winnerUsername = null;

        if (p1TotalScore > p2TotalScore) {
            winnerUserId = player1.getUserId();
            winnerUsername = player1.getUsername();
        } else if (p2TotalScore > p1TotalScore) {
            winnerUserId = player2.getUserId();
            winnerUsername = player2.getUsername();
        }

        BattleDTO.FinalResultNotify finalResult = new BattleDTO.FinalResultNotify();
        finalResult.matchId = matchId;
        finalResult.isDraw = isDraw;
        finalResult.winnerUserId = winnerUserId;
        finalResult.winnerUsername = winnerUsername;

        finalResult.player1 = new BattleDTO.PlayerRoundInfo();
        finalResult.player1.userId = player1.getUserId();
        finalResult.player1.username = player1.getUsername();
        finalResult.player1.totalScore = p1TotalScore;

        finalResult.player2 = new BattleDTO.PlayerRoundInfo();
        finalResult.player2.userId = player2.getUserId();
        finalResult.player2.username = player2.getUsername();
        finalResult.player2.totalScore = p2TotalScore;

        Packet finalPacket = new Packet(OpCode.BATTLE_FINAL_RESULT_NOTIFY, JsonUtil.toJson(finalResult));
        player1.sendPacket(finalPacket);
        player2.sendPacket(finalPacket);

        userDAO.updateMatchResult(player1.getUserId(), p1TotalScore > p2TotalScore, isDraw, p1TotalScore);
        userDAO.updateMatchResult(player2.getUserId(), p2TotalScore > p1TotalScore, isDraw, p2TotalScore);

        scheduler.shutdown();
    }

    // khi có 1 ng bị disconnet
    public synchronized void handleDisconnect(long disconnectedUserId) {
        if (matchFinished)
            return;
        matchFinished = true;

        ClientSession winner = (disconnectedUserId == player1.getUserId()) ? player2 : player1;

        winner.sendPacket(new Packet(
                OpCode.BATTLE_OPPONENT_DISCONNECTED,
                "{\"message\":\"Đối thủ đã mất kết nối hoặc thoát trận. Bạn được xử thắng!\"}"));

        userDAO.updateMatchResult(winner.getUserId(), true, false, 100);

        scheduler.shutdown();
    }

    public long getMatchId() {
        return matchId;
    }

    public ClientSession getPlayer1() {
        return player1;
    }

    public ClientSession getPlayer2() {
        return player2;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public int getBaseScore() {
        return baseScore;
    }
}
