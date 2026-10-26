package com.quizzlive.shared.dto;

import java.io.Serializable;
import java.util.List;

public class TournamentDTO {
    public static class TournamentItem implements Serializable {
        public long tournamentId;
        public String title;
        public String description;
        public String status;
        public int maxParticipants;
        public int currentParticipants;
        public String startTime;
    }

    public static class StandingItem implements Serializable {
        public int rank;
        public String username;
        public String groupName;
        public int matchesPlayed;
        public int wins;
        public int points;
    }

    public static class TournamentDetailResponse implements Serializable {
        public TournamentItem tournament;
        public List<StandingItem> standings;
    }
}
