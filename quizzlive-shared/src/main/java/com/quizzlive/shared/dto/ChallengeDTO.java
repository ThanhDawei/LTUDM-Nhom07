package com.quizzlive.shared.dto;

import java.io.Serializable;

public class ChallengeDTO {
    public static class InviteRequest implements Serializable {
        public long targetUserId;
        public Integer topicId;
        public String difficulty;
    }

    public static class InviteNotify implements Serializable {
        public long invitationId;
        public long fromUserId;
        public String fromUsername;
        public String fromFullName;
        public Integer topicId;
        public String difficulty;
    }

    public static class RespondRequest implements Serializable {
        public long invitationId;
        public boolean accepted;
    }
}
