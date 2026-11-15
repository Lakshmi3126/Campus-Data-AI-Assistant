package com.campusassistant.service;

import com.campusassistant.dto.QueryHistoryDTO;
import com.campusassistant.entity.QueryHistory;
import com.campusassistant.repository.QueryHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QueryHistoryService {

    private final QueryHistoryRepository repository;

    public QueryHistoryService(QueryHistoryRepository repository) {
        this.repository = repository;
    }

    public QueryHistory recordHistory(String question, String generatedSql, String explanation,
                                      String status, Long executionTimeMs, Integer rowCount,
                                      String errorMessage) {
        QueryHistory history = new QueryHistory(
                question,
                generatedSql != null ? generatedSql : "",
                explanation,
                status,
                executionTimeMs != null ? executionTimeMs : 0L,
                rowCount != null ? rowCount : 0,
                errorMessage
        );
        return repository.save(history);
    }

    public List<QueryHistoryDTO> getRecentHistory(int limit) {
        return repository.findRecent(limit)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<QueryHistoryDTO> getHistoryById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    private QueryHistoryDTO toDTO(QueryHistory entity) {
        QueryHistoryDTO dto = new QueryHistoryDTO();
        dto.setId(entity.getId());
        dto.setQuestion(entity.getQuestion());
        dto.setGeneratedSql(entity.getGeneratedSql());
        dto.setExplanation(entity.getExplanation());
        dto.setExecutionStatus(entity.getExecutionStatus());
        dto.setExecutionTimeMs(entity.getExecutionTimeMs());
        dto.setRowCount(entity.getRowCount());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
