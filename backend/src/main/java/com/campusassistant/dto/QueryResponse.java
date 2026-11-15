package com.campusassistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResponse {

    private String question;
    private String generatedSql;
    private String explanation;
    private String queryType;
    private int rowCount;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private String summary;
    private long executionTimeMs;
    private String status; // SUCCESS, ERROR, VALIDATION_FAILED
    private String errorMessage;
    private String conversationId;

    public QueryResponse() {}

    public static QueryResponse success(String question, String generatedSql, String explanation,
                                        String queryType, List<String> columns, List<Map<String, Object>> rows,
                                        String summary, long executionTimeMs, String conversationId) {
        QueryResponse res = new QueryResponse();
        res.setQuestion(question);
        res.setGeneratedSql(generatedSql);
        res.setExplanation(explanation);
        res.setQueryType(queryType);
        res.setColumns(columns);
        res.setRows(rows);
        res.setRowCount(rows != null ? rows.size() : 0);
        res.setSummary(summary);
        res.setExecutionTimeMs(executionTimeMs);
        res.setStatus("SUCCESS");
        res.setConversationId(conversationId);
        return res;
    }

    public static QueryResponse error(String question, String generatedSql, String errorMessage,
                                      String status, long executionTimeMs, String conversationId) {
        QueryResponse res = new QueryResponse();
        res.setQuestion(question);
        res.setGeneratedSql(generatedSql);
        res.setErrorMessage(errorMessage);
        res.setStatus(status);
        res.setExecutionTimeMs(executionTimeMs);
        res.setConversationId(conversationId);
        return res;
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

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
