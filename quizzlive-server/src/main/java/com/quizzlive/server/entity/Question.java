package com.quizzlive.server.entity;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private Long questionId;
    private Integer topicId;
    private String topicName;
    private String questionType; // STANDARD, IQ_IMAGE
    private String difficulty;   // EASY, MEDIUM, HARD
    private String content;
    private String image;
    private String explanation;
    private String status;       // ACTIVE, INACTIVE
    private List<Answer> answers = new ArrayList<>();

    public Question() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
}
