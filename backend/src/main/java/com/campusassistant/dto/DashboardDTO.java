package com.campusassistant.dto;

import java.math.BigDecimal;

public class DashboardDTO {
    private long totalStudents;
    private long totalDepartments;
    private BigDecimal averageCgpa;
    private long totalPlaced;
    private double placementRate;
    private BigDecimal highestPackageLpa;
    private BigDecimal averagePackageLpa;
    private long totalAttendanceRecords;
    private double overallAttendanceRate;

    public DashboardDTO() {}

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public BigDecimal getAverageCgpa() {
        return averageCgpa;
    }

    public void setAverageCgpa(BigDecimal averageCgpa) {
        this.averageCgpa = averageCgpa;
    }

    public long getTotalPlaced() {
        return totalPlaced;
    }

    public void setTotalPlaced(long totalPlaced) {
        this.totalPlaced = totalPlaced;
    }

    public double getPlacementRate() {
        return placementRate;
    }

    public void setPlacementRate(double placementRate) {
        this.placementRate = placementRate;
    }

    public BigDecimal getHighestPackageLpa() {
        return highestPackageLpa;
    }

    public void setHighestPackageLpa(BigDecimal highestPackageLpa) {
        this.highestPackageLpa = highestPackageLpa;
    }

    public BigDecimal getAveragePackageLpa() {
        return averagePackageLpa;
    }

    public void setAveragePackageLpa(BigDecimal averagePackageLpa) {
        this.averagePackageLpa = averagePackageLpa;
    }

    public long getTotalAttendanceRecords() {
        return totalAttendanceRecords;
    }

    public void setTotalAttendanceRecords(long totalAttendanceRecords) {
        this.totalAttendanceRecords = totalAttendanceRecords;
    }

    public double getOverallAttendanceRate() {
        return overallAttendanceRate;
    }

    public void setOverallAttendanceRate(double overallAttendanceRate) {
        this.overallAttendanceRate = overallAttendanceRate;
    }
}
