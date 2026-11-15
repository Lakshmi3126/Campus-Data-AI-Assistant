package com.campusassistant.controller;

import com.campusassistant.dto.OpenAIStructuredResponse;
import com.campusassistant.dto.QueryRequest;
import com.campusassistant.dto.QueryResponse;
import com.campusassistant.exception.SQLValidationException;
import com.campusassistant.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private final SQLGenerationService sqlGenerationService;
    private final SQLValidationService sqlValidationService;
    private final QueryExecutionService queryExecutionService;
    private final OpenAIService openAIService;
    private final QueryHistoryService queryHistoryService;

    public QueryController(SQLGenerationService sqlGenerationService,
                           SQLValidationService sqlValidationService,
                           QueryExecutionService queryExecutionService,
                           OpenAIService openAIService,
                           QueryHistoryService queryHistoryService) {
        this.sqlGenerationService = sqlGenerationService;
        this.sqlValidationService = sqlValidationService;
        this.queryExecutionService = queryExecutionService;
        this.openAIService = openAIService;
        this.queryHistoryService = queryHistoryService;
    }

    @PostMapping
    public ResponseEntity<QueryResponse> processQuery(@Valid @RequestBody QueryRequest request) {
        long startTime = System.currentTimeMillis();
        String conversationId = request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString();

        logger.info("Received query request [convId={}]: '{}'", conversationId, request.getQuestion());

        OpenAIStructuredResponse aiResponse = null;
        String validatedSql = null;

        try {
            // 1. Generate SQL from natural language using OpenAI
            aiResponse = sqlGenerationService.generateSqlForQuestion(request);

            // 2. Validate and enforce safety constraints on SQL query
            validatedSql = sqlValidationService.validateAndEnforceLimit(aiResponse.getGeneratedSql());

            // 3. Execute validated query against MySQL
            QueryExecutionService.ExecutionResult result = queryExecutionService.executeQuery(validatedSql);

            // 4. Generate AI summary from actual retrieved records
            String summary = openAIService.generateSummary(request.getQuestion(), validatedSql, result.getRows());

            long totalTimeMs = System.currentTimeMillis() - startTime;

            // 5. Persist success to query history
            queryHistoryService.recordHistory(
                    request.getQuestion(),
                    validatedSql,
                    aiResponse.getExplanation(),
                    "SUCCESS",
                    totalTimeMs,
                    result.getRows().size(),
                    null
            );

            // 6. Return response
            QueryResponse response = QueryResponse.success(
                    request.getQuestion(),
                    validatedSql,
                    aiResponse.getExplanation(),
                    aiResponse.getQueryType(),
                    result.getColumns(),
                    result.getRows(),
                    summary,
                    totalTimeMs,
                    conversationId
            );

            return ResponseEntity.ok(response);

        } catch (SQLValidationException e) {
            long totalTimeMs = System.currentTimeMillis() - startTime;
            String badSql = aiResponse != null ? aiResponse.getGeneratedSql() : null;
            queryHistoryService.recordHistory(
                    request.getQuestion(),
                    badSql != null ? badSql : "N/A",
                    aiResponse != null ? aiResponse.getExplanation() : null,
                    "VALIDATION_FAILED",
                    totalTimeMs,
                    0,
                    e.getMessage()
            );
            logger.warn("Query rejected by security validator: {}", e.getMessage());
            throw e;

                } catch (IllegalArgumentException e) {
            long totalTimeMs = System.currentTimeMillis() - startTime;

            queryHistoryService.recordHistory(
                    request.getQuestion(),
                    "N/A",
                    null,
                    "REJECTED",
                    totalTimeMs,
                    0,
                    e.getMessage()
            );

            logger.warn("Query rejected: {}", e.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
         catch (Exception e) {
            long totalTimeMs = System.currentTimeMillis() - startTime;
            queryHistoryService.recordHistory(
                    request.getQuestion(),
                    validatedSql != null ? validatedSql : (aiResponse != null ? aiResponse.getGeneratedSql() : "N/A"),
                    aiResponse != null ? aiResponse.getExplanation() : null,
                    "FAILED",
                    totalTimeMs,
                    0,
                    e.getMessage()
            );
            logger.error("Failed to process query: {}", e.getMessage(), e);
            throw e;
        }
    }
}
