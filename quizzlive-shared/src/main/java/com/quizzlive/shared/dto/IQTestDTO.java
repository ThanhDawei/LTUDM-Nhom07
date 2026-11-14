package com.quizzlive.shared.dto;

import java.io.Serializable;
import java.util.List;

public class IQTestDTO {
    public static class IQStartRequest implements Serializable {
        public Integer topicId;
        public String difficulty;
    }

    public static class IQQuestionNotify implements Serializable {
        public long questionId;
        public String content;
        public String imageUrl;
        public String topicName;
        public String difficulty;
        public int currentQuestionIndex; // Câu số mấy trong ngày (1 - 5)
        public int maxDailyQuestions = 5; // Giới hạn 5 câu / ngày
        public boolean isDailyCompleted;  // Đã hoàn thành 5/5 câu trong ngày
        public String dailyMessage;
        public List<BattleDTO.QuestionAnswerOption> options;
    }

    public static class IQSubmitRequest implements Serializable {
        public long questionId;
        public long selectedAnswerId;
    }

    public static class IQResultNotify implements Serializable {
        public boolean isFinished;        // Đã hoàn thành bài test 5 câu trong ngày
        public boolean isCorrect;         // Kết quả của câu vừa làm
        public int currentQuestionIndex;  // Câu vừa làm (1..5)
        public int maxDailyQuestions = 5; // 5 câu
        public int correctCount;          // Số câu đúng
        public int wrongCount;            // Số câu sai
        public double deltaIq;            // Điểm IQ thay đổi (+1.0 - wrongCount*0.2)
        public double currentIqScore;     // Điểm IQ mới sau khi kết toán
        public String message;
    }
}
