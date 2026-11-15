package com.campusassistant.service;

import com.campusassistant.dto.AttendanceAnalyticsDTO;
import com.campusassistant.dto.DashboardDTO;
import com.campusassistant.dto.DepartmentAnalyticsDTO;
import com.campusassistant.dto.PlacementAnalyticsDTO;
import com.campusassistant.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public DashboardDTO getDashboardStats() {
        return analyticsRepository.getDashboardStats();
    }

    public List<DepartmentAnalyticsDTO> getDepartmentAnalytics() {
        return analyticsRepository.getDepartmentAnalytics();
    }

    public List<AttendanceAnalyticsDTO> getAttendanceAnalytics() {
        return analyticsRepository.getAttendanceAnalytics();
    }

    public List<PlacementAnalyticsDTO> getPlacementAnalytics() {
        return analyticsRepository.getPlacementAnalytics();
    }
}
