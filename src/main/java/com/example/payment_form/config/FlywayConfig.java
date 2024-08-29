package com.example.payment_form.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    @DependsOn("databaseInitializer")
    public Flyway flyway(DataSource dataSource) {
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.migrate();
        return flyway;
    }
}
