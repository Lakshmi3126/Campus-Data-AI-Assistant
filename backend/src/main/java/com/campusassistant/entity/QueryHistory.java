package com.campusassistant.entity;

import java.time.Instant;

public class QueryHistory {
    private Long id;
    private String question;
    private String generatedSql;
    private String explanation;
    private String executionStatus;
    private Long executionTimeMs;
    private Integer rowCount;
    private String errorMessage;
    private Instant createdAt;

    public QueryHistory() {}

    public QueryHistory(String question, String generatedSql, String explanation,
                        String executionStatus, Long executionTimeMs, Integer rowCount,
                        String errorMessage) {
        this.question = question;
        this.generatedSql = generatedSql;
        this.explanation = explanation;
        this.executionStatus = executionStatus;
        this.executionTimeMs = executionTimeMs;
        this.rowCount = rowCount;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getGeneratedSql() {
        return generatedSql;
    }

    public void setGeneratedSql(String generatedSql) {
        this.generatedSql = generatedSql;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
