package com.campusassistant.dto;

import java.math.BigDecimal;

public class DepartmentAnalyticsDTO {
    private int departmentId;
    private String departmentName;
    private String departmentCode;
    private long studentCount;
    private BigDecimal averageCgpa;
    private long placedCount;

    public DepartmentAnalyticsDTO() {}

    public DepartmentAnalyticsDTO(int departmentId, String departmentName, String departmentCode,
                                  long studentCount, BigDecimal averageCgpa, long placedCount) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.studentCount = studentCount;
        this.averageCgpa = averageCgpa;
        this.placedCount = placedCount;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    public BigDecimal getAverageCgpa() {
        return averageCgpa;
    }

    public void setAverageCgpa(BigDecimal averageCgpa) {
        this.averageCgpa = averageCgpa;
    }

    public long getPlacedCount() {
        return placedCount;
    }

    public void setPlacedCount(long placedCount) {
        this.placedCount = placedCount;
    }
}
