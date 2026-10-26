package com.quizzlive.server.entity;

public class MatchConfig {
    private Integer configId;
    private String configName;
    private int questionCount;
    private int timePerQuestion;
    private int baseScore;
    private boolean speedBonus;
    private int wrongPenalty;
    private int noAnswerPenalty;
    private boolean active;

    public MatchConfig() {
        this.questionCount = 5;
        this.timePerQuestion = 10;
        this.baseScore = 100;
        this.speedBonus = true;
        this.wrongPenalty = 0;
        this.noAnswerPenalty = 0;
        this.active = true;
    }

    public Integer getConfigId() { return configId; }
    public void setConfigId(Integer configId) { this.configId = configId; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getTimePerQuestion() { return timePerQuestion; }
    public void setTimePerQuestion(int timePerQuestion) { this.timePerQuestion = timePerQuestion; }

    public int getBaseScore() { return baseScore; }
    public void setBaseScore(int baseScore) { this.baseScore = baseScore; }

    public boolean isSpeedBonus() { return speedBonus; }
    public void setSpeedBonus(boolean speedBonus) { this.speedBonus = speedBonus; }

    public int getWrongPenalty() { return wrongPenalty; }
    public void setWrongPenalty(int wrongPenalty) { this.wrongPenalty = wrongPenalty; }

    public int getNoAnswerPenalty() { return noAnswerPenalty; }
    public void setNoAnswerPenalty(int noAnswerPenalty) { this.noAnswerPenalty = noAnswerPenalty; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
