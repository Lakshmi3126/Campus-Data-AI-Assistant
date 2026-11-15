package com.campusassistant.service;

import com.campusassistant.dto.OpenAIStructuredResponse;
import com.campusassistant.dto.QueryRequest;
import com.campusassistant.exception.OpenAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SQLGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(SQLGenerationService.class);

    private final OpenAIService openAIService;
    private final SchemaService schemaService;

    public SQLGenerationService(OpenAIService openAIService, SchemaService schemaService) {
            this.openAIService = openAIService;
            this.schemaService = schemaService;
        }
        private boolean isDestructiveRequest(String question) {
        String normalized = question.toLowerCase().trim();

        return normalized.matches(
            ".*\\b(delete|remove|erase|drop|truncate|update|modify|insert|"
            + "create|alter)\\b.*"
        );
    }

    public OpenAIStructuredResponse generateSqlForQuestion(QueryRequest request) {
        if (isDestructiveRequest(request.getQuestion())) {
            throw new IllegalArgumentException(
                "This assistant only supports read-only database queries. " +
                "Delete, update, insert, and schema modification requests are not allowed."
            );
        }
        String schemaPrompt = schemaService.getSchemaDescriptionForPrompt();
        logger.info("Generating SQL for natural language question: {}", request.getQuestion());

        try {
            // 1. Primary path: Call OpenAI API directly with configured model and prompt
            return openAIService.generateSql(
                    schemaPrompt,
                    request.getQuestion(),
                    request.getPreviousQuestion(),
                    request.getPreviousSql()
            );
        } catch (OpenAIException e) {
            logger.warn("OpenAI API call unavailable ({}: {}). Activating intelligent resilient fallback planner.",
                    e.getClass().getSimpleName(), e.getMessage());

            // 2. Resilient fallback path: Schema-aware NLP query planner
            return generateFallbackSql(request, e.getMessage());
        }
    }

    /**
     * Resilient schema-aware query planner that translates natural language campus queries
     * when upstream OpenAI credentials or network are temporarily unavailable.
     */
    private OpenAIStructuredResponse generateFallbackSql(QueryRequest request, String aiErrorReason) {
        String q = request.getQuestion().trim();
        String lower = q.toLowerCase(Locale.ROOT);
        String fallbackNotice = " [Generated via Schema-Aware Query Planner - Upstream AI Note: " + truncateNotice(aiErrorReason) + "]";

        
    // 1. Follow-up queries (Contextual memory)
    if (request.getPreviousSql() != null && !request.getPreviousSql().isBlank()) {

        boolean isFollowUp =
                lower.contains("above") ||
                lower.contains("greater than") ||
                lower.contains("more than") ||
                lower.contains("at least") ||
                lower.contains("below") ||
                lower.contains("less than") ||
                lower.contains("among them") ||
                lower.contains("these students") ||
                lower.contains("filter");

        if (isFollowUp && (lower.contains("cgpa") || lower.contains("above")
                || lower.contains("below"))) {

            Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(lower);

            if (matcher.find()) {
                String threshold = matcher.group(1);

                String prev = request.getPreviousSql()
                        .replaceAll("(?i)\\s+LIMIT\\s+\\d+", "")
                        .replaceAll("(?i)\\s+ORDER\\s+BY\\s+[^;]+", "")
                        .trim();

                String newSql;

                if (prev.toUpperCase(Locale.ROOT).contains("WHERE")) {
                    newSql = prev + " AND s.cgpa >= " + threshold;
                } else {
                    newSql = prev + " WHERE s.cgpa >= " + threshold;
                }

                newSql += " ORDER BY s.cgpa DESC LIMIT 100";

                return new OpenAIStructuredResponse(
                        newSql,
                        "Contextual follow-up query: filtered previous student list for CGPA >= "
                                + threshold + "." + fallbackNotice,
                        "SELECT"
                );
            }
        }
    }


        // 2. Department-specific students (e.g., "Show all students belonging to the CSE department")
        String deptCode = extractDeptCode(lower);
        if (deptCode != null && (lower.contains("student") || lower.contains("belonging") || lower.contains("list"))) {
            if (lower.contains("average") && lower.contains("cgpa")) {
                String sql = "SELECT d.department_name, d.department_code, ROUND(AVG(s.cgpa), 2) AS average_cgpa " +
                             "FROM students s JOIN departments d ON s.department_id = d.department_id " +
                             "WHERE UPPER(d.department_code) = '" + deptCode + "' GROUP BY d.department_name, d.department_code";
                return new OpenAIStructuredResponse(
                        sql,
                        "Calculates the average CGPA for the " + deptCode + " department." + fallbackNotice,
                        "SELECT"
                );
            }

            String sql = "SELECT s.student_id, s.usn, s.name, d.department_name, s.semester, s.cgpa, s.email " +
                         "FROM students s JOIN departments d ON s.department_id = d.department_id " +
                         "WHERE UPPER(d.department_code) = '" + deptCode + "' ORDER BY s.student_id ASC LIMIT 100";
            return new OpenAIStructuredResponse(
                    sql,
                    "Retrieves all students enrolled in " + deptCode + " department by joining students and departments tables." + fallbackNotice,
                    "SELECT"
            );
        }

        // 3. Attendance below threshold (e.g., "Find students whose attendance is below 75%")
        if (lower.contains("attendance") && (lower.contains("below") || lower.contains("less") || lower.contains("<") || lower.contains("shortage"))) {
            double threshold = 75.0;
            Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(lower);
            if (matcher.find()) {
                try {
                    threshold = Double.parseDouble(matcher.group(1));
                } catch (NumberFormatException ignored) {}
            }
            String sql = "SELECT s.student_id, s.usn, s.name, d.department_code, a.subject, a.attendance_percentage " +
                         "FROM students s " +
                         "JOIN attendance a ON s.student_id = a.student_id " +
                         "JOIN departments d ON s.department_id = d.department_id " +
                         "WHERE a.attendance_percentage < " + threshold + " " +
                         "ORDER BY a.attendance_percentage ASC LIMIT 100";
            return new OpenAIStructuredResponse(
                    sql,
                    "Identifies students whose subject attendance is critically below " + threshold + "%." + fallbackNotice,
                    "SELECT"
            );
        }

        // 4. Average CGPA of all students or per department
        if (lower.contains("average") && lower.contains("cgpa")) {
            String sql = "SELECT d.department_name, d.department_code, ROUND(AVG(s.cgpa), 2) AS average_cgpa " +
                         "FROM students s JOIN departments d ON s.department_id = d.department_id " +
                         "GROUP BY d.department_name, d.department_code ORDER BY average_cgpa DESC";
            return new OpenAIStructuredResponse(
                    sql,
                    "Computes the average student CGPA aggregated across each academic department." + fallbackNotice,
                    "SELECT"
            );
        }

        // 5. Top students based on CGPA (e.g., "Show the top 10 students based on CGPA")
        if (lower.contains("top") && lower.contains("cgpa")) {
            int limit = 10;
            Matcher matcher = Pattern.compile("top\\s+(\\d+)").matcher(lower);
            if (matcher.find()) {
                try {
                    limit = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {}
            }
            String sql = "SELECT s.student_id, s.usn, s.name, d.department_code, s.semester, s.cgpa " +
                         "FROM students s JOIN departments d ON s.department_id = d.department_id " +
                         "ORDER BY s.cgpa DESC LIMIT " + limit;
            return new OpenAIStructuredResponse(
                    sql,
                    "Selects the top " + limit + " highest achieving students ranked by Cumulative GPA." + fallbackNotice,
                    "SELECT"
            );
        }

        // 6. Department with highest number of students
        if (lower.contains("highest") && (lower.contains("student") || lower.contains("number"))) {
            String sql = "SELECT d.department_name, d.department_code, COUNT(s.student_id) AS student_count " +
                         "FROM departments d LEFT JOIN students s ON d.department_id = s.department_id " +
                         "GROUP BY d.department_name, d.department_code ORDER BY student_count DESC LIMIT 1";
            return new OpenAIStructuredResponse(
                    sql,
                    "Finds the academic department having the maximum student enrollment." + fallbackNotice,
                    "SELECT"
            );
        }

        // 7. Placement eligibility (e.g., "Find students eligible for placements")
        if (lower.contains("placement") || lower.contains("eligible")) {
            if (lower.contains("placed") || lower.contains("package") || lower.contains("company")) {
                String sql = "SELECT s.name, d.department_code, p.company_name, p.package_lpa, p.placement_status " +
                             "FROM placements p " +
                             "JOIN students s ON p.student_id = s.student_id " +
                             "JOIN departments d ON s.department_id = d.department_id " +
                             "ORDER BY p.package_lpa DESC LIMIT 100";
                return new OpenAIStructuredResponse(
                        sql,
                        "Displays placement records with company names and compensation packages (LPA)." + fallbackNotice,
                        "SELECT"
                );
            }
            String sql = "SELECT s.student_id, s.usn, s.name, d.department_code, s.semester, s.cgpa, s.email " +
                         "FROM students s JOIN departments d ON s.department_id = d.department_id " +
                         "WHERE s.semester IN (7, 8) AND s.cgpa >= 7.00 ORDER BY s.cgpa DESC LIMIT 100";
            return new OpenAIStructuredResponse(
                    sql,
                    "Filters final-year students (semesters 7 & 8) with CGPA >= 7.00 who meet campus placement eligibility standards." + fallbackNotice,
                    "SELECT"
            );
        }

        // 8. Average marks for each department
        if (lower.contains("marks") || lower.contains("score")) {
            String sql = "SELECT d.department_name, d.department_code, ROUND(AVG(m.marks), 2) AS average_marks " +
                         "FROM departments d " +
                         "JOIN students s ON d.department_id = s.department_id " +
                         "JOIN marks m ON s.student_id = m.student_id " +
                         "GROUP BY d.department_name, d.department_code ORDER BY average_marks DESC";
            return new OpenAIStructuredResponse(
                    sql,
                    "Calculates average academic exam marks broken down by department." + fallbackNotice,
                    "SELECT"
            );
        }

        // 9. All departments
        if (lower.contains("department")) {
            String sql = "SELECT department_id, department_name, department_code FROM departments ORDER BY department_id ASC LIMIT 100";
            return new OpenAIStructuredResponse(
                    sql,
                    "Retrieves all academic departments." + fallbackNotice,
                    "SELECT"
            );
        }

        // 10. Default student directory query
        String sql = "SELECT s.student_id, s.usn, s.name, d.department_code, s.semester, s.cgpa, s.email " +
                     "FROM students s JOIN departments d ON s.department_id = d.department_id ORDER BY s.student_id ASC LIMIT 100";
        return new OpenAIStructuredResponse(
                sql,
                "Retrieves student records with department information." + fallbackNotice,
                "SELECT"
        );
    }

    private String extractDeptCode(String query) {
        if (query.contains("cse") || query.contains("computer")) return "CSE";
        if (query.contains("ise") || query.contains("information")) return "ISE";
        if (query.contains("ece") || query.contains("electronic")) return "ECE";
        if (query.contains("ai") || query.contains("data science")) return "AI_DS";
        if (query.contains("mech") || query.contains("mechanical")) return "MECH";
        if (query.contains("civil")) return "CIVIL";
        return null;
    }

    private String truncateNotice(String error) {
        if (error == null) return "AI service unavailable";
        if (error.contains("401")) return "OpenAI 401 Invalid Key";
        if (error.length() > 60) return error.substring(0, 57) + "...";
        return error;
    }
}
