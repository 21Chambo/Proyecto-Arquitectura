package com.backend.unab.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.HttpStatus;

class HealthControllerTest {
    private DataSource database;
    private ApplicationAvailability availability;
    private HealthController controller;

    @BeforeEach
    void setup() {
        database = mock(DataSource.class);
        availability = mock(ApplicationAvailability.class);
        controller = new HealthController(database, "test-version", availability);
    }

    @Test
    void refusesTrafficUntilStartupRunnersFinish() {
        when(availability.getReadinessState()).thenReturn(ReadinessState.REFUSING_TRAFFIC);
        var response = controller.health();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("starting", response.getBody().get("status"));
        verifyNoInteractions(database);
    }

    @Test
    void reportsHealthyOnlyWhenReadyAndDatabaseResponds() throws Exception {
        when(availability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(database.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        var response = controller.health();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody().get("status"));
        assertEquals("test-version", response.getBody().get("version"));
        verify(statement).execute("SELECT 1");
    }

    @Test
    void databaseFailureReturns503() throws Exception {
        when(availability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        when(database.getConnection()).thenThrow(new SQLException("unavailable"));
        var response = controller.health();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("error", response.getBody().get("status"));
    }
}
