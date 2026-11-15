package com.campusassistant.service;

import com.campusassistant.exception.SQLValidationException;
import com.campusassistant.security.SQLSecurityProperties;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SQLValidationService {

    private static final Logger logger = LoggerFactory.getLogger(SQLValidationService.class);

    private final SQLSecurityProperties securityProperties;

    // Prohibited keywords and dangerous functions
    private static final List<String> DISALLOWED_KEYWORDS = List.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE", "CREATE",
            "REPLACE", "RENAME", "EXEC", "EXECUTE", "CALL", "GRANT", "REVOKE",
            "SHUTDOWN", "INTO OUTFILE", "INTO DUMPFILE", "LOAD DATA", "SLEEP",
            "BENCHMARK", "INFORMATION_SCHEMA", "PERFORMANCE_SCHEMA", "SYS."
    );

    private static final Pattern COMMENT_PATTERN = Pattern.compile("(--|/\\*|\\*/|#)");

    public SQLValidationService(SQLSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Validates and sanitizes the SQL query.
     * Enforces SELECT-only, single statement, allowed tables, and limit enforcement.
     *
     * @param rawSql The raw SQL query to validate.
     * @return The sanitized and safety-enforced SQL query string.
     * @throws SQLValidationException if the query violates any security policy.
     */
    public String validateAndEnforceLimit(String rawSql) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            throw new SQLValidationException("SQL query cannot be null or empty.");
        }

        String cleanedSql = cleanSqlString(rawSql);

        // 1. Check for dangerous keywords in raw text as first defensive line
        String upperSql = cleanedSql.toUpperCase(Locale.ROOT);
        for (String disallowed : DISALLOWED_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(disallowed) + "\\b");
            if (pattern.matcher(upperSql).find()) {
                throw new SQLValidationException(
                        "Security Violation: Prohibited keyword or operation detected: '" + disallowed + "'",
                        rawSql
                );
            }
        }

        // 2. Reject comment injection attempts
        if (COMMENT_PATTERN.matcher(cleanedSql).find()) {
            throw new SQLValidationException(
                    "Security Violation: SQL comments are not permitted in generated queries.",
                    rawSql
            );
        }

        // 3. Parse AST with JSqlParser
        Statement statement;
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(cleanedSql);
            if (statements.getStatements().size() != 1) {
                throw new SQLValidationException(
                        "Security Violation: Multiple SQL statements are strictly forbidden.",
                        rawSql
                );
            }
            statement = statements.getStatements().get(0);
        } catch (JSQLParserException e) {
            throw new SQLValidationException(
                    "Syntax Error: Failed to parse SQL query: " + e.getMessage(),
                    rawSql
            );
        }

        // 4. Ensure statement is strictly a SELECT
        if (!(statement instanceof Select select)) {
            throw new SQLValidationException(
                    "Security Violation: Only SELECT queries are permitted. Attempted: " + statement.getClass().getSimpleName(),
                    rawSql
            );
        }

        // 5. Verify all referenced tables against allowed whitelist
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList((Statement) select);

        Set<String> allowedTables = securityProperties.getAllowedTableSet();
        for (String table : tableList) {
            String sanitizedTable = table.replace("`", "").toLowerCase(Locale.ROOT);
            if (!allowedTables.contains(sanitizedTable)) {
                throw new SQLValidationException(
                        "Security Violation: Access to table '" + table + "' is not permitted. Allowed tables: " + allowedTables,
                        rawSql
                );
            }
        }

        // 6. Enforce and clamp LIMIT
        enforceLimit(select);

        String validatedSql = select.toString();
        logger.info("SQL Validation PASSED. Executable query: {}", validatedSql);
        return validatedSql;
    }

    private void enforceLimit(Select select) {
        int maxRows = securityProperties.getMaxRows();

        if (select.getSelectBody() instanceof PlainSelect plainSelect) {
            Limit limit = plainSelect.getLimit();
            if (limit == null) {
                Limit newLimit = new Limit();
                newLimit.setRowCount(new LongValue(maxRows));
                plainSelect.setLimit(newLimit);
            } else if (limit.getRowCount() instanceof LongValue longValue) {
                if (longValue.getValue() > maxRows) {
                    longValue.setValue(maxRows);
                }
            }
        }
    }

    private String cleanSqlString(String sql) {
        String trimmed = sql.trim();
        // Remove markdown code fences if AI wrapped it in ```sql ... ```
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }

        // Remove trailing semicolon for AST normalization
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        return trimmed;
    }
}
