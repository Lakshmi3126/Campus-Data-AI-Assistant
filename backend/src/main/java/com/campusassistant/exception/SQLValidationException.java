package com.campusassistant.exception;

public class SQLValidationException extends AppException {
    private final String attemptedSql;

    public SQLValidationException(String message) {
        super(message);
        this.attemptedSql = null;
    }

    public SQLValidationException(String message, String attemptedSql) {
        super(message);
        this.attemptedSql = attemptedSql;
    }

    public String getAttemptedSql() {
        return attemptedSql;
    }
}
