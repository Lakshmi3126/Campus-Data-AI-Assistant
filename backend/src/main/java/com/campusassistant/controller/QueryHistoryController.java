package com.campusassistant.controller;

import com.campusassistant.dto.QueryHistoryDTO;
import com.campusassistant.exception.ResourceNotFoundException;
import com.campusassistant.service.QueryHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class QueryHistoryController {

    private final QueryHistoryService queryHistoryService;

    public QueryHistoryController(QueryHistoryService queryHistoryService) {
        this.queryHistoryService = queryHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<QueryHistoryDTO>> getRecentHistory(
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        int effectiveLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(queryHistoryService.getRecentHistory(effectiveLimit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueryHistoryDTO> getHistoryById(@PathVariable("id") Long id) {
        return queryHistoryService.getHistoryById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Query history record not found with ID: " + id));
    }
}
