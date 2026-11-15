package com.campusassistant.service;

import com.campusassistant.config.OpenAIConfig;
import com.campusassistant.dto.OpenAIStructuredResponse;
import com.campusassistant.exception.OpenAIException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private final RestClient openAiRestClient;
    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;

    public OpenAIService(RestClient openAiRestClient, OpenAIConfig openAIConfig, ObjectMapper objectMapper) {
        this.openAiRestClient = openAiRestClient;
        this.openAIConfig = openAIConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates SQL, explanation, and query type using OpenAI API with Structured Output.
     */
    public OpenAIStructuredResponse generateSql(String schemaDescription, String userQuestion,
                                                String previousQuestion, String previousSql) {
        if (openAIConfig.getApiKey() == null || openAIConfig.getApiKey().isBlank()) {
            throw new OpenAIException("OpenAI API key is missing. Please configure OPENAI_API_KEY environment variable.");
        }

        String systemPrompt = buildSystemPrompt(schemaDescription);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Inject conversational history if follow-up question
        if (previousQuestion != null && !previousQuestion.isBlank() && previousSql != null && !previousSql.isBlank()) {
            messages.add(Map.of("role", "user", "content", previousQuestion));
            messages.add(Map.of("role", "assistant", "content", String.format(
                    "{\"generatedSql\": \"%s\", \"explanation\": \"Previous query.\", \"queryType\": \"SELECT\"}",
                    previousSql.replace("\"", "\\\"").replace("\n", " ")
            )));
        }

        messages.add(Map.of("role", "user", "content", userQuestion));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAIConfig.getModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
        requestBody.put("response_format", Map.of("type", "json_object"));

        try {
            logger.info("Calling OpenAI model '{}' for question: {}", openAIConfig.getModel(), userQuestion);

            String responseBody = openAiRestClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        String errorText = "";
                        try {
                            errorText = new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        } catch (Exception ex) {
                            errorText = "Could not read error body: " + ex.getMessage();
                        }
                        logger.error("OpenAI API error response ({}): {}", response.getStatusCode(), errorText);
                        throw new OpenAIException("OpenAI API returned error (" + response.getStatusCode() + "): " + errorText);
                    })
                    .body(String.class);

            return parseStructuredResponse(responseBody);
        } catch (OpenAIException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to communicate with OpenAI API: {}", e.getMessage(), e);
            throw new OpenAIException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    
    /**
     * Generates a concise and accurate summary based on executed SQL and database results.
     */
    public String generateSummary(String question, String sql, List<Map<String, Object>> rows) {

        if (rows == null || rows.isEmpty()) {
            return "No matching records were found in the database for the given criteria.";
        }

        if (openAIConfig.getApiKey() == null || openAIConfig.getApiKey().isBlank()) {
            return "Retrieved " + rows.size() + " matching records from the database.";
        }

        try {
            // Include all rows when there are 20 or fewer.
            // For larger results, send a sample and disclose that it is partial.
            boolean allRowsIncluded = rows.size() <= 20;

            List<Map<String, Object>> data = allRowsIncluded
                    ? rows
                    : rows.subList(0, 10);

            String dataJson = objectMapper.writeValueAsString(data);

            String prompt = """
                    You are a precise campus database analyst.

                    Summarize the executed SQL query results accurately in 2-3 concise sentences.

                    STRICT RULES:
                    1. Use ONLY the supplied SQL, question and database results.
                    2. Never invent student names, department names, numbers or statistics.
                    3. Account for every returned record when all records are supplied.
                    4. Never omit a returned department or record when asked to identify all qualifying results.
                    5. Respect all conditions already enforced by the SQL query.
                    6. If the SQL contains a HAVING condition, acknowledge that the returned groups
                    have already passed that condition.
                    7. Never claim that additional data is needed to verify a condition that
                    the executed SQL has already enforced.
                    8. Distinguish between the total number of returned records and the number
                    of records included in the supplied data.
                    9. If only a sample is supplied, do not claim that the sample contains
                    every matching record.
                    10. If no records are returned, state that no matching records were found.

                    Original user question:
                    """ + question + """

                    Executed SQL:
                    """ + sql + """

                    Total rows returned by database: """ + rows.size() + """

                    All rows included: """ + allRowsIncluded + """

                    Database results:
                    """ + dataJson + """

                    Write a factual summary that directly answers the user's question.
                    """;

            Map<String, Object> requestBody = Map.of(
                    "model", openAIConfig.getModel(),
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are a precise analytical assistant. Never contradict the supplied database results."
                            ),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 300
            );

            String responseBody = openAiRestClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);

            String summary = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            if (summary.isBlank()) {
                throw new OpenAIException("OpenAI returned an empty summary.");
            }

            return summary;

        } catch (Exception e) {
            logger.warn("Could not generate AI summary (fallback used): {}", e.getMessage());

            return "Successfully retrieved " + rows.size()
                    + " records from the database matching your criteria.";
        }
    }


    private OpenAIStructuredResponse parseStructuredResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                throw new OpenAIException("OpenAI returned no completion choices.");
            }

            String content = choices.get(0).path("message").path("content").asText();
            logger.debug("Raw OpenAI content response: {}", content);

            // Parse inner JSON
            JsonNode contentNode = objectMapper.readTree(content);

            String sql = contentNode.path("generatedSql").asText("");
            String explanation = contentNode.path("explanation").asText("Query generated based on schema.");
            String queryType = contentNode.path("queryType").asText("SELECT");

            if (sql.isBlank()) {
                // Fallback attempt to see if top-level fields were named differently
                if (contentNode.has("sql")) {
                    sql = contentNode.get("sql").asText();
                } else if (contentNode.has("query")) {
                    sql = contentNode.get("query").asText();
                }
            }

            if (sql.isBlank()) {
                throw new OpenAIException("OpenAI structured response did not contain a generatedSql property.");
            }

            return new OpenAIStructuredResponse(sql, explanation, queryType);
        } catch (OpenAIException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to parse OpenAI JSON response: {}", e.getMessage(), e);
            throw new OpenAIException("Failed to parse structured response from AI model: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String schemaDescription) {
        return """
            You are an expert AI Database Architect and SQL Generator for a university campus management system.
            Your role is to convert natural language queries into safe, standard MySQL SELECT queries.
            
            """ + schemaDescription + """
            
            STRICT GENERATION RULES:
            1. ONLY generate SELECT queries. Never generate INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE, or schema modifications.
            2. Never generate multiple SQL statements or semicolon-separated stacks.
            3. Only use the allowed tables: students, departments, attendance, marks, placements.
            4. Join tables correctly using proper foreign keys:
               - students.department_id = departments.department_id
               - attendance.student_id = students.student_id
               - marks.student_id = students.student_id
               - placements.student_id = students.student_id
            5. Use meaningful column aliases (e.g. AVG(s.cgpa) AS average_cgpa).
            6. For department filtering, check both department_name and department_code case-insensitively (e.g. UPPER(d.department_code) = 'CSE' OR UPPER(d.department_name) LIKE '%COMPUTER%').
            7. For attendance below 75%, filter WHERE a.attendance_percentage < 75.00.
            8. Always return your response in strictly valid JSON format with the following exact keys:
               {
                 "generatedSql": "SELECT ... FROM ...",
                 "explanation": "Clear explanation in plain English of what the query does and which tables/filters were used.",
                 "queryType": "SELECT"
               }
            9. Do not include markdown ticks, conversational preamble, or explanations outside the JSON object.

            10. CONVERSATIONAL FOLLOW-UP RULES:
                - When a user asks a follow-up question, treat the previous SQL query as the
                starting point for the new query.
                - Preserve ALL existing WHERE conditions and filters unless the user
                explicitly asks to remove or change them.
                - Preserve the previous ORDER BY clause unless the user asks for a
                different sorting order.
                - Preserve the previous LIMIT value unless the user explicitly asks
                for a different number of results.
                - Preserve existing JOINs, GROUP BY and HAVING clauses unless the
                follow-up requires a change.
                - When the user asks to add a column, add the required column and JOIN
                without unnecessarily changing existing query conditions.
                - When adding a new filter, combine it with existing filters using AND
                and use parentheses when mixing AND and OR.
                - Never replace an existing LIMIT with 100 or another default value
                when a previous LIMIT already exists.
                - Modify only the parts of the previous query that the user's
                follow-up request requires.

                
            11. AGGREGATION AND JOIN RULES:
                - When the user asks for results for EACH or EVERY department,
                  return all departments unless a filter is explicitly requested.
                - When the user asks to INCLUDE departments even if they have
                  no matching records, use LEFT JOIN.
                - Start the query from the table whose records must all be preserved.
                - Do not use INNER JOIN if it would exclude records the user
                  explicitly wants included.
                - Do not add LIMIT 1 when the user asks for results for each department.
                - Use GROUP BY for department-level aggregate results.
                - For departments with no attendance records, preserve the
                  department and allow AVG(a.attendance_percentage) to return NULL.
                - Do not silently exclude departments with no matching students
                  or attendance records.

            12. Avoid using SQL reserved keywords as column aliases.
            13. Use descriptive aliases such as student_rank instead of rank.
            14. When using window functions, ensure the SQL syntax is compatible
                with MySQL 8.0+.

            15. For ranking queries, use RANK() OVER
                (PARTITION BY ... ORDER BY ...) when ranking within groups.

            16. MYSQL WINDOW FUNCTION RESTRICTIONS:
                - Never use a window function directly in WHERE, GROUP BY,
                or HAVING clauses.
                - Window functions may be used in SELECT and ORDER BY.
                - If filtering based on a window function result, use a
                CTE or derived table and filter in the outer SELECT.
                - Never compare a column directly with a window function
                inside WHERE.

            17. DEPARTMENT AVERAGE COMPARISONS:
                - When asked to find students whose CGPA is above their
                department's average, use a correlated subquery:
                WHERE s.cgpa > (
                    SELECT AVG(s2.cgpa)
                    FROM students s2
                    WHERE s2.department_id = s.department_id
                )
                - Do not use AVG() OVER (PARTITION BY ...) directly
                in the WHERE clause for this comparison.

            18. Always generate executable MySQL 8.0+ SQL.


            """;
    }
}
