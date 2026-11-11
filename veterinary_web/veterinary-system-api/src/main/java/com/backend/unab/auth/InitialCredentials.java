package com.backend.unab.auth;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Replaces only the original V0002 seed hashes, never a customized password. */
@Component
public class InitialCredentials implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialCredentials.class);
    static final String ADMIN_SEED = "$2a$10$uwq8TV6jQp9qLBi9LWO1ReITVk.VNgTHbVXeeheD9CPnE9C.9J/CG";
    static final String STAFF_SEED = "$2a$10$hWcLinFMnlTYzPVe471G5.ohKV8XikIDP086mfa5HjiiYqJYn8nVW";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String staffPassword;

    public InitialCredentials(JdbcTemplate jdbc, PasswordEncoder encoder,
            @Value("${APP_INITIAL_ADMIN_PASSWORD:}") String adminPassword,
            @Value("${APP_INITIAL_STAFF_PASSWORD:}") String staffPassword) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.staffPassword = staffPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        Map<String, String> seeds = new LinkedHashMap<>();
        seeds.put("admin", ADMIN_SEED);
        seeds.put("staff", STAFF_SEED);
        Map<String, String> passwords = new LinkedHashMap<>();
        passwords.put("admin", adminPassword);
        passwords.put("staff", staffPassword);
        Map<String, String> replacements = new LinkedHashMap<>();

        // Validate both accounts before updating either one.
        for (Map.Entry<String, String> seed : seeds.entrySet()) {
            String username = seed.getKey();
            List<String> stored = jdbc.queryForList(
                    "SELECT password FROM users WHERE username = ?", String.class, username);
            if (stored.isEmpty()) {
                throw new IllegalStateException("Missing initial account " + username + "; run Flyway migrations first.");
            }
            if (!seed.getValue().equals(stored.get(0))) {
                continue;
            }
            String password = passwords.get(username);
            String variable = "APP_INITIAL_" + username.toUpperCase(java.util.Locale.ROOT) + "_PASSWORD";
            if (password == null || password.trim().isEmpty() || password.length() < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException(variable + " must contain at least 12 characters and at most 72 UTF-8 bytes.");
            }
            if (encoder.matches(password, seed.getValue())) {
                throw new IllegalStateException(variable + " must differ from the original seed password.");
            }
            replacements.put(username, encoder.encode(password));
        }

        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            // The old hash in the WHERE clause also protects concurrent password changes.
            int updated = jdbc.update("UPDATE users SET password = ? WHERE username = ? AND password = ?",
                    replacement.getValue(), replacement.getKey(), seeds.get(replacement.getKey()));
            if (updated > 0) {
                LOGGER.info("Initialized password for account {}", replacement.getKey());
            }
        }
    }
}
