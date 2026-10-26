package com.quizzlive.shared.dto;

import java.io.Serializable;
import java.util.List;

public class IQTestDTO {
    public static class IQQuestionNotify implements Serializable {
        public long questionId;
        public String content;
        public String imageUrl;
        public List<BattleDTO.QuestionAnswerOption> options;
    }

    public static class IQSubmitRequest implements Serializable {
        public long questionId;
        public long selectedAnswerId;
    }

    public static class IQResultNotify implements Serializable {
        public double currentIqScore;
        public boolean isCorrect;
        public String message;
    }
}
