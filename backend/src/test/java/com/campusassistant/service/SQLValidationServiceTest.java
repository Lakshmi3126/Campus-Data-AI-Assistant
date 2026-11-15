package com.campusassistant.service;

import com.campusassistant.exception.SQLValidationException;
import com.campusassistant.security.SQLSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SQLValidationServiceTest {

    private SQLValidationService validationService;

    @BeforeEach
    void setUp() {
        SQLSecurityProperties securityProperties = new SQLSecurityProperties();
        securityProperties.setMaxRows(100);
        securityProperties.setAllowedTables("students,departments,attendance,marks,placements,query_history");
        validationService = new SQLValidationService(securityProperties);
    }

    @Test
    @DisplayName("Should accept valid SELECT query and enforce LIMIT 100")
    void testValidSelectQuery() {
        String sql = "SELECT student_id, name, cgpa FROM students WHERE cgpa > 8.5";
        String validated = validationService.validateAndEnforceLimit(sql);

        assertNotNull(validated);
        assertTrue(validated.toUpperCase().contains("LIMIT 100"));
        assertTrue(validated.toUpperCase().startsWith("SELECT"));
    }

    @Test
    @DisplayName("Should accept complex valid JOIN query")
    void testValidJoinQuery() {
        String sql = "SELECT s.name, d.department_name, s.cgpa " +
                     "FROM students s " +
                     "JOIN departments d ON s.department_id = d.department_id " +
                     "WHERE d.department_code = 'CSE' ORDER BY s.cgpa DESC";
        String validated = validationService.validateAndEnforceLimit(sql);

        assertNotNull(validated);
        assertTrue(validated.toUpperCase().contains("LIMIT 100"));
    }

    @Test
    @DisplayName("Should clamp LIMIT if greater than maximum allowed (100)")
    void testClampExcessiveLimit() {
        String sql = "SELECT * FROM students LIMIT 500";
        String validated = validationService.validateAndEnforceLimit(sql);

        assertTrue(validated.toUpperCase().contains("LIMIT 100"));
        assertFalse(validated.toUpperCase().contains("LIMIT 500"));
    }

    @Test
    @DisplayName("Should preserve LIMIT if smaller than maximum allowed")
    void testPreserveSmallerLimit() {
        String sql = "SELECT * FROM students LIMIT 10";
        String validated = validationService.validateAndEnforceLimit(sql);

        assertTrue(validated.toUpperCase().contains("LIMIT 10"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DELETE FROM students",
            "DROP TABLE students",
            "UPDATE students SET cgpa = 10.0",
            "INSERT INTO students (name) VALUES ('Hacker')",
            "ALTER TABLE students ADD COLUMN hacked VARCHAR(100)",
            "TRUNCATE TABLE students",
            "CREATE TABLE evil (id INT)"
    })
    @DisplayName("Should reject destructive DML and DDL commands")
    void testRejectDestructiveStatements(String destructiveSql) {
        assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(destructiveSql));
    }

    @Test
    @DisplayName("Should reject multiple SQL statements (SQL injection)")
    void testRejectMultipleStatements() {
        String multiSql = "SELECT * FROM students; DROP TABLE departments;";
        assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(multiSql));
    }

    @Test
    @DisplayName("Should reject access to unauthorized tables")
    void testRejectUnauthorizedTable() {
        String sql = "SELECT * FROM users";
        SQLValidationException ex = assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(sql));
        assertTrue(ex.getMessage().contains("not permitted"));
    }

    @Test
    @DisplayName("Should reject access to system metadata tables")
    void testRejectSystemTables() {
        String sql = "SELECT table_name FROM information_schema.tables";
        assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(sql));
    }

    @Test
    @DisplayName("Should reject SQL comments and injection attempts")
    void testRejectCommentInjections() {
        String commentSql = "SELECT * FROM students -- malicious comment";
        assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(commentSql));
    }

    @Test
    @DisplayName("Should reject sleep and benchmark functions")
    void testRejectDangerousFunctions() {
        String sleepSql = "SELECT SLEEP(5) FROM students";
        assertThrows(SQLValidationException.class, () -> validationService.validateAndEnforceLimit(sleepSql));
    }
}
