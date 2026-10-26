package com.quizzlive.shared.dto;

import java.io.Serializable;

public class TopicDTO implements Serializable {
    public int topicId;
    public String topicName;
    public String description;

    public TopicDTO() {}

    public TopicDTO(int topicId, String topicName, String description) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.description = description;
    }
}
