package com.campusassistant.controller;

import com.campusassistant.config.OpenAIConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final OpenAIConfig openAIConfig;

    public HealthController(JdbcTemplate jdbcTemplate, OpenAIConfig openAIConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.openAIConfig = openAIConfig;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        health.put("service", "Campus Data AI Assistant Backend");

        // Database health check
        Map<String, Object> dbHealth = new HashMap<>();
        try {
            Integer dbTest = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (dbTest != null && dbTest == 1) {
                dbHealth.put("status", "CONNECTED");
            } else {
                dbHealth.put("status", "DEGRADED");
            }
        } catch (Exception e) {
            dbHealth.put("status", "DISCONNECTED");
            dbHealth.put("error", e.getMessage());
            health.put("status", "DEGRADED");
        }
        health.put("database", dbHealth);

        // OpenAI config check
        Map<String, Object> aiHealth = new HashMap<>();
        boolean hasKey = openAIConfig.getApiKey() != null && !openAIConfig.getApiKey().isBlank();
        aiHealth.put("configured", hasKey);
        aiHealth.put("model", openAIConfig.getModel());
        health.put("aiService", aiHealth);

        return ResponseEntity.ok(health);
    }
}
