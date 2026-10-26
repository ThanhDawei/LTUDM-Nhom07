package com.quizzlive.server.entity;

public class Answer {
    private Long answerId;
    private Long questionId;
    private String keySymbol; // A, B, C, D
    private String content;
    private String image;
    private boolean correct;

    public Answer() {}

    public Answer(Long answerId, Long questionId, String keySymbol, String content, boolean correct) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.keySymbol = keySymbol;
        this.content = content;
        this.correct = correct;
    }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getKeySymbol() { return keySymbol; }
    public void setKeySymbol(String keySymbol) { this.keySymbol = keySymbol; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
