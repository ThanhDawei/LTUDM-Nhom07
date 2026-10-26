package com.quizzlive.shared.dto;

import java.io.Serializable;
import java.util.List;

public class BattleDTO {
    public static class QuestionAnswerOption implements Serializable {
        public long answerId;
        public String key; // A, B, C, D
        public String content;
        public String image;
    }

    public static class BattleQuestionNotify implements Serializable {
        public long matchId;
        public int questionIndex; // 1 -> 5
        public int totalQuestions; // 5
        public int timeLimitSeconds; // 10
        public String questionContent;
        public String questionImage;
        public List<QuestionAnswerOption> options;
    }

    public static class AnswerSubmitRequest implements Serializable {
        public long matchId;
        public int questionIndex;
        public Long selectedAnswerId; // null nếu hết giờ
        public int responseTimeSeconds; // Thời gian người chơi trả lời
    }

    public static class PlayerRoundInfo implements Serializable {
        public long userId;
        public String username;
        public Long selectedAnswerId;
        public boolean isCorrect;
        public int scoreEarned;
        public int totalScore;
    }

    public static class RoundResultNotify implements Serializable {
        public long matchId;
        public int questionIndex;
        public long correctAnswerId;
        public String explanation;
        public PlayerRoundInfo player1;
        public PlayerRoundInfo player2;
    }

    public static class FinalResultNotify implements Serializable {
        public long matchId;
        public Long winnerUserId; // null nếu hòa
        public String winnerUsername;
        public PlayerRoundInfo player1;
        public PlayerRoundInfo player2;
        public boolean isDraw;
    }
}
