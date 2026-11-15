package com.campusassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Verifying database schema and initial data...");
            boolean tablesExist = false;

            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'students'",
                        Integer.class
                );
                tablesExist = (count != null && count > 0);
            } catch (Exception e) {
                logger.warn("Could not check information_schema for existing tables: {}", e.getMessage());
            }

            if (!tablesExist) {
                logger.info("Initializing schema from schema.sql...");
                ResourceDatabasePopulator schemaPopulator = new ResourceDatabasePopulator();
                schemaPopulator.addScript(new ClassPathResource("schema.sql"));
                schemaPopulator.setContinueOnError(false);
                schemaPopulator.execute(dataSource);
                logger.info("Schema initialized successfully.");
            }

            // Check if students table has rows
            Integer studentCount = 0;
            try {
                studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
            } catch (Exception e) {
                // Table might not exist yet if schema creation failed
            }

            if (studentCount == null || studentCount == 0) {
                logger.info("Database contains no student records. Seeding synthetic data from data.sql...");
                ResourceDatabasePopulator dataPopulator = new ResourceDatabasePopulator();
                dataPopulator.addScript(new ClassPathResource("data.sql"));
                dataPopulator.setContinueOnError(true);
                dataPopulator.execute(dataSource);
                logger.info("Synthetic sample data seeded successfully.");
            } else {
                logger.info("Database already initialized with {} student records.", studentCount);
            }
        } catch (Exception e) {
            logger.error("Database initialization check encountered an error: {}", e.getMessage(), e);
        }
    }
}
