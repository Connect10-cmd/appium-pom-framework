package com.tests;

/**
 * TestDataConstants — Centralized test credentials and data.
 * Single source of truth for test user accounts and scenarios.
 * Update here instead of scattering across test methods.
 */
public class TestDataConstants {

    // Valid test user credentials (from login_data.json)
    public static final String VALID_USERNAME = "valid_user@test.com";
    public static final String VALID_PASSWORD = "Valid@Pass1";

    public static final String ADMIN_USERNAME = "admin@test.com";
    public static final String ADMIN_PASSWORD = "Admin@Pass1";

    // Invalid/edge case credentials
    public static final String INVALID_USERNAME = "wrong@test.com";
    public static final String INVALID_PASSWORD = "WrongPassword!";

    public static final String NONEXISTENT_USERNAME = "doesnotexist_xyz999@test.com";
    public static final String NONEXISTENT_PASSWORD = "AnyPass1!";

    // Security test payloads
    public static final String SQL_INJECTION_PAYLOAD = "' OR '1'='1";
    public static final String XSS_PAYLOAD = "<script>alert('xss')</script>";

    // Boundary test values
    public static final String LONG_INPUT = "a".repeat(512);

    private TestDataConstants() {
        /* Static utility class */
    }
}
