package com.quizzlive.shared.dto;

import java.io.Serializable;
import java.util.List;

public class LeaderboardDTO {
    public static class LeaderboardItem implements Serializable {
        public int rank;
        public long userId;
        public String username;
        public String fullName;
        public int totalMatches;
        public int winMatches;
        public double winRate;
        public int currentStreak;
        public int maxStreak;
        public long totalPoints;
        public double iqScore;
    }

    public static class LeaderboardResponse implements Serializable {
        public List<LeaderboardItem> topPlayers;
        public List<LeaderboardItem> topStreaks;
        public List<LeaderboardItem> mostPlayed;
    }

    public static class MatchHistoryItem implements Serializable {
        public long matchId;
        public String matchCode;
        public String opponentUsername;
        public String result; // WIN, LOSS, DRAW
        public int myScore;
        public int opponentScore;
        public String playedAt;
    }
}
