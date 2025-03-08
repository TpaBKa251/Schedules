package ru.tpu.hostel.schedules.repository.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("resource")
public class PostgresTestContainerExtension implements BeforeAllCallback, AfterAllCallback {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        POSTGRES.stop();
    }
}