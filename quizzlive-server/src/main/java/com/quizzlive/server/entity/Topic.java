package com.quizzlive.server.entity;

public class Topic {
    private Integer topicId;
    private String topicName;
    private String description;
    private boolean active;

    public Topic() {}

    public Topic(Integer topicId, String topicName, String description, boolean active) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.description = description;
        this.active = active;
    }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
