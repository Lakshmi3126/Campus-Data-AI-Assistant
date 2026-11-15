package com.campusassistant.dto;

public class AttendanceAnalyticsDTO {
    private String bracket;
    private long count;
    private double percentage;

    public AttendanceAnalyticsDTO() {}

    public AttendanceAnalyticsDTO(String bracket, long count, double percentage) {
        this.bracket = bracket;
        this.count = count;
        this.percentage = percentage;
    }

    public String getBracket() {
        return bracket;
    }

    public void setBracket(String bracket) {
        this.bracket = bracket;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
