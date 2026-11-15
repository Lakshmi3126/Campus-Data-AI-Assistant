package com.campusassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QueryRequest {

    @NotBlank(message = "Question cannot be blank")
    @Size(max = 1000, message = "Question cannot exceed 1000 characters")
    private String question;

    private String conversationId;

    private String previousSql;

    private String previousQuestion;

    public QueryRequest() {}

    public QueryRequest(String question) {
        this.question = question;
    }

    public QueryRequest(String question, String conversationId) {
        this.question = question;
        this.conversationId = conversationId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getPreviousSql() {
        return previousSql;
    }

    public void setPreviousSql(String previousSql) {
        this.previousSql = previousSql;
    }

    public String getPreviousQuestion() {
        return previousQuestion;
    }

    public void setPreviousQuestion(String previousQuestion) {
        this.previousQuestion = previousQuestion;
    }
}
