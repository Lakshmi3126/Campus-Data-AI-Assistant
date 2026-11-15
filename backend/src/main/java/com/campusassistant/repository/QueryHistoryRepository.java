package com.campusassistant.repository;

import com.campusassistant.entity.QueryHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class QueryHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QueryHistory> rowMapper = (rs, rowNum) -> {
        QueryHistory history = new QueryHistory();
        history.setId(rs.getLong("id"));
        history.setQuestion(rs.getString("question"));
        history.setGeneratedSql(rs.getString("generated_sql"));
        history.setExplanation(rs.getString("explanation"));
        history.setExecutionStatus(rs.getString("execution_status"));
        history.setExecutionTimeMs(rs.getLong("execution_time_ms"));
        history.setRowCount(rs.getInt("row_count"));
        history.setErrorMessage(rs.getString("error_message"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            history.setCreatedAt(ts.toInstant());
        }
        return history;
    };

    public QueryHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QueryHistory save(QueryHistory history) {
        String sql = "INSERT INTO query_history (question, generated_sql, explanation, execution_status, execution_time_ms, row_count, error_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, history.getQuestion());
            ps.setString(2, history.getGeneratedSql());
            ps.setString(3, history.getExplanation());
            ps.setString(4, history.getExecutionStatus());
            ps.setLong(5, history.getExecutionTimeMs() != null ? history.getExecutionTimeMs() : 0);
            ps.setInt(6, history.getRowCount() != null ? history.getRowCount() : 0);
            ps.setString(7, history.getErrorMessage());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            history.setId(keyHolder.getKey().longValue());
        }
        return history;
    }

    public List<QueryHistory> findRecent(int limit) {
        String sql = "SELECT id, question, generated_sql, explanation, execution_status, execution_time_ms, row_count, error_message, created_at " +
                     "FROM query_history ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public Optional<QueryHistory> findById(Long id) {
        String sql = "SELECT id, question, generated_sql, explanation, execution_status, execution_time_ms, row_count, error_message, created_at " +
                     "FROM query_history WHERE id = ?";
        List<QueryHistory> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
