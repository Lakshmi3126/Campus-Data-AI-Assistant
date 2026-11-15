package com.campusassistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIStructuredResponse {

    @JsonProperty("generatedSql")
    private String generatedSql;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("queryType")
    private String queryType;

    public OpenAIStructuredResponse() {}

    public OpenAIStructuredResponse(String generatedSql, String explanation, String queryType) {
        this.generatedSql = generatedSql;
        this.explanation = explanation;
        this.queryType = queryType;
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
}
