package com.campusassistant.dto;

import java.math.BigDecimal;

public class PlacementAnalyticsDTO {
    private String companyName;
    private long offersCount;
    private BigDecimal averagePackageLpa;
    private BigDecimal maxPackageLpa;

    public PlacementAnalyticsDTO() {}

    public PlacementAnalyticsDTO(String companyName, long offersCount, BigDecimal averagePackageLpa, BigDecimal maxPackageLpa) {
        this.companyName = companyName;
        this.offersCount = offersCount;
        this.averagePackageLpa = averagePackageLpa;
        this.maxPackageLpa = maxPackageLpa;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getOffersCount() {
        return offersCount;
    }

    public void setOffersCount(long offersCount) {
        this.offersCount = offersCount;
    }

    public BigDecimal getAveragePackageLpa() {
        return averagePackageLpa;
    }

    public void setAveragePackageLpa(BigDecimal averagePackageLpa) {
        this.averagePackageLpa = averagePackageLpa;
    }

    public BigDecimal getMaxPackageLpa() {
        return maxPackageLpa;
    }

    public void setMaxPackageLpa(BigDecimal maxPackageLpa) {
        this.maxPackageLpa = maxPackageLpa;
    }
}
