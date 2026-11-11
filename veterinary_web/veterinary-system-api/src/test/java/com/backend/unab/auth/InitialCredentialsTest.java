package com.backend.unab.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class InitialCredentialsTest {
    private EmbeddedDatabase database;
    private JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void seedDatabase() {
        database = new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        jdbc = new JdbcTemplate(database);
        jdbc.execute("CREATE TABLE users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(255), enabled BOOLEAN)");
        jdbc.update("INSERT INTO users VALUES (?, ?, ?)", "admin", InitialCredentials.ADMIN_SEED, true);
        jdbc.update("INSERT INTO users VALUES (?, ?, ?)", "staff", InitialCredentials.STAFF_SEED, true);
    }

    @AfterEach
    void closeDatabase() {
        database.shutdown();
    }

    @Test
    void initializesBothSeedAccountsWithBcrypt() {
        initializer("Admin-Test-2026!", "Staff-Test-2026!").run(null);
        assertTrue(encoder.matches("Admin-Test-2026!", password("admin")));
        assertTrue(encoder.matches("Staff-Test-2026!", password("staff")));
        assertNotEquals("Admin-Test-2026!", password("admin"));
    }

    @Test
    void restartsDoNotResetPasswordsEvenIfEnvironmentChanges() {
        initializer("Admin-Test-2026!", "Staff-Test-2026!").run(null);
        String admin = password("admin");
        String staff = password("staff");
        initializer("Other-Admin-2026!", "Other-Staff-2026!").run(null);
        assertEquals(admin, password("admin"));
        assertEquals(staff, password("staff"));
    }

    @Test
    void preservesCustomizedAccountsAndDisabledStatus() {
        String customAdmin = encoder.encode("Existing-Admin-2026!");
        jdbc.update("UPDATE users SET password = ?, enabled = FALSE WHERE username = 'admin'", customAdmin);
        initializer("", "Staff-Test-2026!").run(null);
        assertEquals(customAdmin, password("admin"));
        assertFalse(jdbc.queryForObject("SELECT enabled FROM users WHERE username = 'admin'", Boolean.class));
        assertTrue(encoder.matches("Staff-Test-2026!", password("staff")));
    }

    @Test
    void missingStaffSecretFailsBeforeChangingAdmin() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> initializer("Admin-Test-2026!", "").run(null));
        assertTrue(error.getMessage().contains("APP_INITIAL_STAFF_PASSWORD"));
        assertEquals(InitialCredentials.ADMIN_SEED, password("admin"));
        assertEquals(InitialCredentials.STAFF_SEED, password("staff"));
    }

    @Test
    void rejectsShortPasswordsAndBcryptTruncation() {
        assertThrows(IllegalStateException.class, () -> initializer("admin123", "Staff-Test-2026!").run(null));
        assertThrows(IllegalStateException.class,
                () -> initializer("\u00e9".repeat(37), "Staff-Test-2026!").run(null));
        assertEquals(InitialCredentials.ADMIN_SEED, password("admin"));
    }

    @Test
    void missingSeedAccountFailsWithoutChangingOtherAccounts() {
        jdbc.update("DELETE FROM users WHERE username = 'staff'");
        assertThrows(IllegalStateException.class,
                () -> initializer("Admin-Test-2026!", "Staff-Test-2026!").run(null));
        assertEquals(InitialCredentials.ADMIN_SEED, password("admin"));
    }

    private InitialCredentials initializer(String admin, String staff) {
        return new InitialCredentials(jdbc, encoder, admin, staff);
    }

    private String password(String username) {
        return jdbc.queryForObject("SELECT password FROM users WHERE username = ?", String.class, username);
    }
}
