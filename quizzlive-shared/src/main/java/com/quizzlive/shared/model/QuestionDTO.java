package com.quizzlive.shared.model;

import java.io.Serializable;
import java.util.List;

public class QuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String content;
    private List<String> options;
    private int timeLimitSeconds;

    public QuestionDTO() {}

    public QuestionDTO(int id, String content, List<String> options, int timeLimitSeconds) {
        this.id = id;
        this.content = content;
        this.options = options;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
}
