package com.quizzlive.server.service;

import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.Answer;
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
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // cấu hình trận đấu
    public static final int TOTAL_QUESTIONS = 5;
    public static final int TIME_LIMIT_SECONDS = 10;
    public static final int BASE_SCORE = 100;

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
    }

    public void start() {
        questions = questionDAO.getRandomQuestions(topicId, difficulty, TOTAL_QUESTIONS);// lấy câu hỏi ngẫu nhiên

        if (questions == null || questions.size() < TOTAL_QUESTIONS) {// nếu không đủ câu hỏi thì lấy câu hỏi ngẫu nhiên
            questions = questionDAO.getRandomQuestions(null, null, TOTAL_QUESTIONS);
        }

        Packet startPacket = new Packet(OpCode.BATTLE_START_NOTIFY, "{\"matchId\":" + matchId + "}");// gửi thông báo
                                                                                                     // bắt đầu trận đấu
        player1.sendPacket(startPacket);
        player2.sendPacket(startPacket);

        scheduler.schedule(this::nextQuestion, 2000, TimeUnit.MILLISECONDS);
    }

    public synchronized void nextQuestion() {
        if (currentQuestionIndex >= TOTAL_QUESTIONS) {
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
        questionNotify.totalQuestions = TOTAL_QUESTIONS;
        questionNotify.timeLimitSeconds = TIME_LIMIT_SECONDS;
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
    }

    // nhận câu hỏi tính điểm và dồng bộ
    public synchronized void submitAnswer(long userId, Long selectedAnswerId, int responseTime) {

    }

    // quá 10s mà có 1 ng ko trl
    private synchronized void onRoundTimeout() {

    }

    private synchronized void onBothAnswered() {

    }

    private synchronized void finishMatch() {

    }

    // khi có 1 ng bị disconnet
    public synchronized void handleDisconnect(long disconnectedUserId) {

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
}
