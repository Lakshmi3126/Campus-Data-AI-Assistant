package com.campusassistant.controller;

import com.campusassistant.dto.OpenAIStructuredResponse;
import com.campusassistant.dto.QueryRequest;
import com.campusassistant.exception.SQLValidationException;
import com.campusassistant.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SQLGenerationService sqlGenerationService;

    @MockBean
    private SQLValidationService sqlValidationService;

    @MockBean
    private QueryExecutionService queryExecutionService;

    @MockBean
    private OpenAIService openAIService;

    @MockBean
    private QueryHistoryService queryHistoryService;

    @Test
    @DisplayName("POST /api/query - should successfully process valid query")
    void testProcessQuerySuccess() throws Exception {
        QueryRequest request = new QueryRequest("Show all students with CGPA above 9");

        OpenAIStructuredResponse mockAiResponse = new OpenAIStructuredResponse(
                "SELECT * FROM students WHERE cgpa > 9.0",
                "Retrieves students whose CGPA exceeds 9.0",
                "SELECT"
        );

        String validatedSql = "SELECT * FROM students WHERE cgpa > 9.0 LIMIT 100";

        QueryExecutionService.ExecutionResult mockResult = new QueryExecutionService.ExecutionResult(
                List.of("student_id", "name", "cgpa"),
                List.of(Map.of("student_id", 1, "name", "Aarav Sharma", "cgpa", 9.45)),
                12L
        );

        when(sqlGenerationService.generateSqlForQuestion(any(QueryRequest.class))).thenReturn(mockAiResponse);
        when(sqlValidationService.validateAndEnforceLimit(anyString())).thenReturn(validatedSql);
        when(queryExecutionService.executeQuery(anyString())).thenReturn(mockResult);
        when(openAIService.generateSummary(anyString(), anyString(), any())).thenReturn("Found 1 high-performing student.");

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.question").value("Show all students with CGPA above 9"))
                .andExpect(jsonPath("$.generatedSql").value(validatedSql))
                .andExpect(jsonPath("$.rowCount").value(1))
                .andExpect(jsonPath("$.rows[0].name").value("Aarav Sharma"));
    }

    @Test
    @DisplayName("POST /api/query - should reject blank question with 400 Bad Request")
    void testBlankQuestionValidation() throws Exception {
        QueryRequest request = new QueryRequest("");

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.question").exists());
    }

    @Test
    @DisplayName("POST /api/query - should return 400 when SQL validation fails")
    void testSqlValidationFailure() throws Exception {
        QueryRequest request = new QueryRequest("Delete all students");

        OpenAIStructuredResponse mockAiResponse = new OpenAIStructuredResponse(
                "DELETE FROM students",
                "Deletes all students",
                "DELETE"
        );

        when(sqlGenerationService.generateSqlForQuestion(any(QueryRequest.class))).thenReturn(mockAiResponse);
        when(sqlValidationService.validateAndEnforceLimit(anyString()))
                .thenThrow(new SQLValidationException("Security Violation: Only SELECT queries are permitted."));

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("SQL Security Validation Failed"));
    }
}
