package com.campusassistant.controller;

import com.campusassistant.dto.AttendanceAnalyticsDTO;
import com.campusassistant.dto.DepartmentAnalyticsDTO;
import com.campusassistant.dto.PlacementAnalyticsDTO;
import com.campusassistant.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/department")
    public ResponseEntity<List<DepartmentAnalyticsDTO>> getDepartmentAnalytics() {
        return ResponseEntity.ok(analyticsService.getDepartmentAnalytics());
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceAnalyticsDTO>> getAttendanceAnalytics() {
        return ResponseEntity.ok(analyticsService.getAttendanceAnalytics());
    }

    @GetMapping("/placements")
    public ResponseEntity<List<PlacementAnalyticsDTO>> getPlacementAnalytics() {
        return ResponseEntity.ok(analyticsService.getPlacementAnalytics());
    }
}
