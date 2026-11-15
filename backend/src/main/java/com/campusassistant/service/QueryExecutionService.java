package com.campusassistant.service;

import com.campusassistant.security.SQLSecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.*;

@Service
public class QueryExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionService.class);

    private final JdbcTemplate jdbcTemplate;
    private final SQLSecurityProperties securityProperties;

    public QueryExecutionService(JdbcTemplate jdbcTemplate, SQLSecurityProperties securityProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.securityProperties = securityProperties;
    }

    public ExecutionResult executeQuery(String validatedSql) {
        logger.info("Executing validated query: {}", validatedSql);
        long startTime = System.currentTimeMillis();

        List<String> columnNames = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try {
            jdbcTemplate.setQueryTimeout(securityProperties.getQueryTimeoutSeconds());

            jdbcTemplate.query(validatedSql, rs -> {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnNames.isEmpty()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = metaData.getColumnLabel(i);
                        if (colName == null || colName.isBlank()) {
                            colName = metaData.getColumnName(i);
                        }
                        columnNames.add(colName);
                    }
                }

                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String colName = columnNames.get(i - 1);
                    Object val = rs.getObject(i);
                    row.put(colName, val);
                }
                rows.add(row);
            });

            long executionTimeMs = System.currentTimeMillis() - startTime;
            logger.info("Query executed successfully in {} ms, returned {} rows", executionTimeMs, rows.size());

            return new ExecutionResult(columnNames, rows, executionTimeMs);
        } catch (DataAccessException e) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            logger.error("Query execution failed after {} ms: {}", executionTimeMs, e.getMessage());
            throw e;
        }
    }

    public static class ExecutionResult {
        private final List<String> columns;
        private final List<Map<String, Object>> rows;
        private final long executionTimeMs;

        public ExecutionResult(List<String> columns, List<Map<String, Object>> rows, long executionTimeMs) {
            this.columns = columns;
            this.rows = rows;
            this.executionTimeMs = executionTimeMs;
        }

        public List<String> getColumns() {
            return columns;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
    }
}
