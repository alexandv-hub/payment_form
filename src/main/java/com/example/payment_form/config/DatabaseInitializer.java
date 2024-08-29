package com.example.payment_form.config;

import com.example.payment_form.exception.DatabaseInitializationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.example.payment_form.messages.ErrorMessages.Database.ERR_DATABASE_INIT_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.name}")
    private String dbName;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void initializeDatabase() {
        log.info("Connecting to PostgreSQL server...");

        var baseDbUrlStr = dbUrl.replace(dbName, "");

        try (Connection connection = DriverManager.getConnection(baseDbUrlStr, dbUsername, dbPassword);
             Statement statement = connection.createStatement()) {
            log.info("Connected to PostgreSQL server successfully.");

            if (!isDatabaseExists(statement)) {
                log.error("No database found.");
                createDatabase(statement);
            }

        } catch (Exception e) {
            log.error(ERR_DATABASE_INIT_FAILED, e);
            throw new DatabaseInitializationException("Database init failed.", e);
        }
    }

    private boolean isDatabaseExists(Statement statement) throws Exception {
        var queryStr = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'";
        ResultSet resultSet = statement.executeQuery(queryStr);
        log.info("isDatabaseExists = {}", resultSet);
        return resultSet.next();
    }

    private void createDatabase(Statement statement) {
        try {
            log.info("Starting to create a new database.");
            statement.executeUpdate("CREATE DATABASE " + dbName);
            log.info("Database successfully created.");
        } catch (Exception e) {
            log.error("Database creation failed.", e);
            throw new RuntimeException(e);
        }
    }
}
