package com.quizzlive.shared.dto;

import java.io.Serializable;

public class MatchmakingDTO {
    public static class QueueRequest implements Serializable {
        public Integer topicId;
        public String difficulty; // EASY, MEDIUM, HARD
    }

    public static class MatchFoundNotify implements Serializable {
        public long matchId;
        public String matchCode;
        public long opponentId;
        public String opponentUsername;
        public String opponentFullName;
        public String opponentAvatar;
        public int totalQuestions;
        public int timeLimitSeconds;
    }
}
