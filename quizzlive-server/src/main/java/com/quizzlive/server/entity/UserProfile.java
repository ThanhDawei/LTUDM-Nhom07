package com.quizzlive.server.entity;

public class UserProfile {
    private Long userId;
    private String fullName;
    private String avatar;
    private String bio;
    private Double iqScore;
    private Integer totalMatches;
    private Integer winMatches;
    private Integer loseMatches;
    private Integer drawMatches;
    private Integer currentStreak;
    private Integer maxStreak;
    private Long totalPoints;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Integer highestScore;

    public UserProfile() {
        this.iqScore = 0.0;
        this.totalMatches = 0;
        this.winMatches = 0;
        this.loseMatches = 0;
        this.drawMatches = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.totalPoints = 0L;
        this.totalQuestions = 0;
        this.correctQuestions = 0;
        this.highestScore = 0;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Double getIqScore() { return iqScore; }
    public void setIqScore(Double iqScore) { this.iqScore = iqScore; }

    public Integer getTotalMatches() { return totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }

    public Integer getWinMatches() { return winMatches; }
    public void setWinMatches(Integer winMatches) { this.winMatches = winMatches; }

    public Integer getLoseMatches() { return loseMatches; }
    public void setLoseMatches(Integer loseMatches) { this.loseMatches = loseMatches; }

    public Integer getDrawMatches() { return drawMatches; }
    public void setDrawMatches(Integer drawMatches) { this.drawMatches = drawMatches; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getMaxStreak() { return maxStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }

    public Long getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Long totalPoints) { this.totalPoints = totalPoints; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getCorrectQuestions() { return correctQuestions; }
    public void setCorrectQuestions(Integer correctQuestions) { this.correctQuestions = correctQuestions; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }
}
