package com.campusassistant.service;

import com.campusassistant.dto.SchemaDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SchemaService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaService.class);

    private final JdbcTemplate jdbcTemplate;
    private SchemaDTO cachedSchema;
    private final Map<String, Set<String>> tableColumnsMap = new HashMap<>();

    public SchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        refreshSchema();
    }

    public synchronized void refreshSchema() {
        try {
            List<SchemaDTO.TableInfo> tables = new ArrayList<>();

            // 1. departments
            tables.add(new SchemaDTO.TableInfo(
                    "departments",
                    "Academic departments within the college",
                    List.of(
                            new SchemaDTO.ColumnInfo("department_id", "INT", true, false, "Primary key, unique identifier for department"),
                            new SchemaDTO.ColumnInfo("department_name", "VARCHAR(100)", false, false, "Full name of department (e.g., Computer Science & Engineering)"),
                            new SchemaDTO.ColumnInfo("department_code", "VARCHAR(10)", false, false, "Short department code (CSE, ISE, ECE, AI_DS, MECH, CIVIL)"),
                            new SchemaDTO.ColumnInfo("created_at", "TIMESTAMP", false, true, "Timestamp when record was created")
                    ),
                    List.of()
            ));

            // 2. students
            tables.add(new SchemaDTO.TableInfo(
                    "students",
                    "Student academic and profile information",
                    List.of(
                            new SchemaDTO.ColumnInfo("student_id", "INT", true, false, "Primary key, internal student ID"),
                            new SchemaDTO.ColumnInfo("usn", "VARCHAR(20)", false, false, "University Seat Number, unique student ID"),
                            new SchemaDTO.ColumnInfo("name", "VARCHAR(100)", false, false, "Full student name"),
                            new SchemaDTO.ColumnInfo("department_id", "INT", false, false, "Foreign key referencing departments.department_id"),
                            new SchemaDTO.ColumnInfo("semester", "INT", false, false, "Current semester (1 to 8)"),
                            new SchemaDTO.ColumnInfo("cgpa", "DECIMAL(3,2)", false, false, "Cumulative Grade Point Average (0.00 to 10.00)"),
                            new SchemaDTO.ColumnInfo("email", "VARCHAR(120)", false, false, "Student email address"),
                            new SchemaDTO.ColumnInfo("created_at", "TIMESTAMP", false, true, "Record creation timestamp")
                    ),
                    List.of(
                            new SchemaDTO.ForeignKeyInfo("department_id", "departments", "department_id")
                    )
            ));

            // 3. attendance
            tables.add(new SchemaDTO.TableInfo(
                    "attendance",
                    "Student attendance records by subject",
                    List.of(
                            new SchemaDTO.ColumnInfo("attendance_id", "INT", true, false, "Primary key"),
                            new SchemaDTO.ColumnInfo("student_id", "INT", false, false, "Foreign key referencing students.student_id"),
                            new SchemaDTO.ColumnInfo("subject", "VARCHAR(100)", false, false, "Subject name (e.g. Database Management Systems)"),
                            new SchemaDTO.ColumnInfo("attendance_percentage", "DECIMAL(5,2)", false, false, "Attendance percentage (0.00 to 100.00)"),
                            new SchemaDTO.ColumnInfo("recorded_at", "TIMESTAMP", false, true, "Timestamp recorded")
                    ),
                    List.of(
                            new SchemaDTO.ForeignKeyInfo("student_id", "students", "student_id")
                    )
            ));

            // 4. marks
            tables.add(new SchemaDTO.TableInfo(
                    "marks",
                    "Student marks obtained in individual subjects",
                    List.of(
                            new SchemaDTO.ColumnInfo("marks_id", "INT", true, false, "Primary key"),
                            new SchemaDTO.ColumnInfo("student_id", "INT", false, false, "Foreign key referencing students.student_id"),
                            new SchemaDTO.ColumnInfo("subject", "VARCHAR(100)", false, false, "Subject name"),
                            new SchemaDTO.ColumnInfo("marks", "DECIMAL(5,2)", false, false, "Marks obtained"),
                            new SchemaDTO.ColumnInfo("maximum_marks", "DECIMAL(5,2)", false, false, "Total maximum marks possible (default 100.00)"),
                            new SchemaDTO.ColumnInfo("exam_type", "VARCHAR(50)", false, true, "Type of exam (e.g., Final Semester)"),
                            new SchemaDTO.ColumnInfo("recorded_at", "TIMESTAMP", false, true, "Timestamp recorded")
                    ),
                    List.of(
                            new SchemaDTO.ForeignKeyInfo("student_id", "students", "student_id")
                    )
            ));

            // 5. placements
            tables.add(new SchemaDTO.TableInfo(
                    "placements",
                    "Campus placement job offers and packages",
                    List.of(
                            new SchemaDTO.ColumnInfo("placement_id", "INT", true, false, "Primary key"),
                            new SchemaDTO.ColumnInfo("student_id", "INT", false, false, "Foreign key referencing students.student_id"),
                            new SchemaDTO.ColumnInfo("company_name", "VARCHAR(100)", false, false, "Recruiting company name"),
                            new SchemaDTO.ColumnInfo("package_lpa", "DECIMAL(4,2)", false, false, "Annual package in Lakhs Per Annum (LPA)"),
                            new SchemaDTO.ColumnInfo("placement_status", "VARCHAR(50)", false, false, "Status: Placed, Offered, etc."),
                            new SchemaDTO.ColumnInfo("placement_date", "DATE", false, true, "Date of placement offer")
                    ),
                    List.of(
                            new SchemaDTO.ForeignKeyInfo("student_id", "students", "student_id")
                    )
            ));

            // 6. query_history
            tables.add(new SchemaDTO.TableInfo(
                    "query_history",
                    "Audit log of natural language queries and generated SQL",
                    List.of(
                            new SchemaDTO.ColumnInfo("id", "BIGINT", true, false, "Primary key"),
                            new SchemaDTO.ColumnInfo("question", "TEXT", false, false, "Natural language question submitted"),
                            new SchemaDTO.ColumnInfo("generated_sql", "TEXT", false, false, "Generated SQL statement"),
                            new SchemaDTO.ColumnInfo("explanation", "TEXT", false, true, "Explanation of query logic"),
                            new SchemaDTO.ColumnInfo("execution_status", "VARCHAR(30)", false, false, "SUCCESS, FAILED, or VALIDATION_FAILED"),
                            new SchemaDTO.ColumnInfo("execution_time_ms", "BIGINT", false, true, "Execution time in milliseconds"),
                            new SchemaDTO.ColumnInfo("row_count", "INT", false, true, "Number of rows returned"),
                            new SchemaDTO.ColumnInfo("error_message", "TEXT", false, true, "Error message if failed"),
                            new SchemaDTO.ColumnInfo("created_at", "TIMESTAMP", false, true, "Timestamp recorded")
                    ),
                    List.of()
            ));

            this.cachedSchema = new SchemaDTO(tables);

            // Populate table-column map
            tableColumnsMap.clear();
            for (SchemaDTO.TableInfo table : tables) {
                Set<String> cols = new HashSet<>();
                for (SchemaDTO.ColumnInfo col : table.getColumns()) {
                    cols.add(col.getColumnName().toLowerCase());
                }
                tableColumnsMap.put(table.getTableName().toLowerCase(), cols);
            }

            logger.info("Database schema metadata successfully loaded for {} tables.", tables.size());
        } catch (Exception e) {
            logger.error("Failed to load schema: {}", e.getMessage(), e);
        }
    }

    public SchemaDTO getSchemaDTO() {
        if (cachedSchema == null) {
            refreshSchema();
        }
        return cachedSchema;
    }

    public boolean isColumnInTable(String tableName, String columnName) {
        Set<String> cols = tableColumnsMap.get(tableName.toLowerCase());
        return cols != null && cols.contains(columnName.toLowerCase());
    }

    public Set<String> getColumnsForTable(String tableName) {
        return tableColumnsMap.getOrDefault(tableName.toLowerCase(), Collections.emptySet());
    }

    public String getSchemaDescriptionForPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are querying a MySQL database named 'campus_data_assistant'.\n");
        sb.append("Database Schema and Table Definitions:\n\n");

        for (SchemaDTO.TableInfo table : getSchemaDTO().getTables()) {
            // Do not expose query_history to normal student/campus queries unless asked
            if (table.getTableName().equalsIgnoreCase("query_history")) {
                continue;
            }

            sb.append("TABLE: ").append(table.getTableName()).append(" (").append(table.getDescription()).append(")\n");
            sb.append("COLUMNS:\n");
            for (SchemaDTO.ColumnInfo col : table.getColumns()) {
                sb.append("  - ").append(col.getColumnName())
                        .append(" (").append(col.getDataType()).append(")")
                        .append(col.isPrimaryKey() ? " [PRIMARY KEY]" : "")
                        .append(": ").append(col.getDescription()).append("\n");
            }

            if (!table.getForeignKeys().isEmpty()) {
                sb.append("RELATIONSHIPS / FOREIGN KEYS:\n");
                for (SchemaDTO.ForeignKeyInfo fk : table.getForeignKeys()) {
                    sb.append("  - ").append(table.getTableName()).append(".").append(fk.getColumnName())
                            .append(" REFERENCES ").append(fk.getReferencedTable()).append("(").append(fk.getReferencedColumn()).append(")\n");
                }
            }
            sb.append("\n");
        }

        sb.append("IMPORTANT DOMAIN RULES:\n");
        sb.append("1. Department Names & Codes: 'Computer Science & Engineering' (code 'CSE'), 'Information Science & Engineering' ('ISE'), 'Electronics & Communication Engineering' ('ECE'), 'Artificial Intelligence & Data Science' ('AI_DS'), 'Mechanical Engineering' ('MECH'), 'Civil Engineering' ('CIVIL').\n");
        sb.append("2. When filtering by department, use either d.department_name OR d.department_code, usually with UPPER() or LIKE for flexibility.\n");
        sb.append("3. Attendance percentage is stored as a number between 0.00 and 100.00 (e.g. 75.00 is 75%).\n");
        sb.append("4. CGPA is on a 10.00 scale (e.g., 8.50, 9.20).\n");
        sb.append("5. Placements are recorded in `placements` table with `package_lpa` (Lakhs Per Annum) and `placement_status` ('Placed'). Students eligible for placements are generally in semester 7 or 8.\n");

        return sb.toString();
    }
}
