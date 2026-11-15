package com.campusassistant.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "sql.security")
public class SQLSecurityProperties {

    private int maxRows = 100;
    private int queryTimeoutSeconds = 10;
    private String allowedTables = "students,departments,attendance,marks,placements,query_history";

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public String getAllowedTables() {
        return allowedTables;
    }

    public void setAllowedTables(String allowedTables) {
        this.allowedTables = allowedTables;
    }

    public Set<String> getAllowedTableSet() {
        Set<String> set = new HashSet<>();
        if (allowedTables != null) {
            Arrays.stream(allowedTables.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(set::add);
        }
        return set;
    }
}
