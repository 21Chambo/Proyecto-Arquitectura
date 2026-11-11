package com.backend.unab.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final String version;
    private final ApplicationAvailability availability;

    public HealthController(DataSource dataSource,
                            @Value("${APP_VERSION:dev}") String version,
                            ApplicationAvailability availability) {
        this.dataSource = dataSource;
        this.version = version;
        this.availability = availability;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> body = new LinkedHashMap<>();
        if (availability.getReadinessState() != ReadinessState.ACCEPTING_TRAFFIC) {
            body.put("status", "starting");
            body.put("version", version);
            return ResponseEntity.status(503).body(body);
        }
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute("SELECT 1");
            body.put("status", "ok");
            body.put("version", version);
            return ResponseEntity.ok(body);
        } catch (SQLException e) {
            body.put("status", "error");
            body.put("version", version);
            return ResponseEntity.status(503).body(body);
        }
    }
}
