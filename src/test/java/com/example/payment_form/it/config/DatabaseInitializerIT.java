package com.example.payment_form.it.config;

import com.example.payment_form.config.DatabaseInitializer;
import com.example.payment_form.exception.DatabaseInitializationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.example.payment_form.messages.ErrorMessages.Database.ERR_DATABASE_INIT_FAILED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public class DatabaseInitializerIT {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private DataSource dataSource;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:latest")
            .withReuse(Boolean.FALSE);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @Test
    void testDatabaseInitialization() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'payment_form_db_test'");
            assertTrue(resultSet.next(), "Database should exist");

            databaseInitializer.initializeDatabase();
        }
    }

    @Test
    void testDatabaseConnectionFailure() {
        var faultyInitializer = new DatabaseInitializer();
        ReflectionTestUtils.setField(faultyInitializer, "dbUrl", postgreSQLContainer.getJdbcUrl().replace("payment_form_db_test", "invalid_db_name"));
        ReflectionTestUtils.setField(faultyInitializer, "dbName", "invalid_db_name");
        ReflectionTestUtils.setField(faultyInitializer, "dbUsername", "invalid_user");
        ReflectionTestUtils.setField(faultyInitializer, "dbPassword", "invalid_password");

        DatabaseInitializationException exception = assertThrows(DatabaseInitializationException.class, faultyInitializer::initializeDatabase);

        var actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(ERR_DATABASE_INIT_FAILED));
    }
}
