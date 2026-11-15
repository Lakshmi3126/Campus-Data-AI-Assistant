package com.campusassistant.controller;

import com.campusassistant.dto.DashboardDTO;
import com.campusassistant.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboardStats() {
        DashboardDTO stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
