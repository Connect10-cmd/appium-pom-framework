package com.framework.utils;

import com.framework.core.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.UnreachableBrowserException;
import java.util.function.Supplier;

/**
 * ExceptionHandler — Centralized exception handling for mobile automation.
 * Provides recovery strategies and meaningful error messages for common Appium issues.
 */
public class ExceptionHandler {

    private ExceptionHandler() { /* Utility class */ }

    /**
     * Execute action with comprehensive exception handling and recovery.
     */
    public static <T> T executeWithRecovery(Supplier<T> action, String actionDescription) {
        try {
            return action.get();
        } catch (Exception e) {
            return handleException(e, actionDescription);
        }
    }

    /**
     * Execute void action with comprehensive exception handling and recovery.
     */
    public static void executeWithRecovery(Runnable action, String actionDescription) {
        try {
            action.run();
        } catch (Exception e) {
            handleException(e, actionDescription);
        }
    }

    /**
     * Handle exceptions with recovery strategies.
     */
    private static <T> T handleException(Exception e, String actionDescription) {
        LogManager.error("Exception during: " + actionDescription, e);

        // Recovery strategies based on exception type
        if (e instanceof StaleElementReferenceException) {
            LogManager.warn("Element became stale during: " + actionDescription + ". Element reference is no longer valid.");
            throw new RuntimeException("Element became stale: " + actionDescription, e);
        }

        if (e instanceof NoSuchElementException) {
            LogManager.error("Element not found: " + actionDescription + ". Check locator strategy and element visibility.", e);
            throw new RuntimeException("Element not found: " + actionDescription, e);
        }

        if (e instanceof TimeoutException) {
            LogManager.error("Timeout waiting for: " + actionDescription + ". Element may not be visible or interactable within timeout.", e);
            throw new RuntimeException("Timeout during: " + actionDescription, e);
        }

        if (e instanceof WebDriverException) {
            handleWebDriverException((WebDriverException) e, actionDescription);
            throw new RuntimeException("WebDriver error during: " + actionDescription, e);
        }

        if (e instanceof UnreachableBrowserException) {
            LogManager.error("Appium server became unreachable during: " + actionDescription + ". Check server status.", e);
            throw new RuntimeException("Appium server unreachable: " + actionDescription, e);
        }

        // Generic exception
        LogManager.error("Unexpected error during: " + actionDescription, e);
        throw new RuntimeException("Unexpected error during: " + actionDescription, e);
    }

    /**
     * Handle WebDriver-specific exceptions.
     */
    private static void handleWebDriverException(WebDriverException e, String actionDescription) {
        String message = e.getMessage().toLowerCase();

        if (message.contains("session not created")) {
            LogManager.error("Session creation failed. Check device availability and capabilities.", e);
        } else if (message.contains("no such session")) {
            LogManager.error("Session was terminated. Driver may have quit unexpectedly.", e);
        } else if (message.contains("element not interactable")) {
            LogManager.error("Element exists but is not interactable. Check if element is obscured or disabled.", e);
        } else if (message.contains("invalid selector")) {
            LogManager.error("Invalid locator strategy. Check element locators.", e);
        } else if (message.contains("unable to capture screen")) {
            LogManager.warn("Screenshot capture failed, but test can continue.");
        } else {
            LogManager.error("WebDriver error: " + e.getMessage(), e);
        }
    }

    /**
     * Check if driver is in a recoverable state.
     */
    public static boolean isDriverRecoverable() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                return false;
            }
            // Try a simple operation to check if driver is responsive
            driver.getPageSource();
            return true;
        } catch (Exception e) {
            LogManager.warn("Driver is not in recoverable state: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user-friendly error message for common issues.
     */
    public static String getUserFriendlyMessage(Exception e) {
        if (e instanceof TimeoutException) {
            return "Operation timed out. The app may be slow or unresponsive.";
        }
        if (e instanceof NoSuchElementException) {
            return "Element not found. The app UI may have changed or element is not visible.";
        }
        if (e instanceof StaleElementReferenceException) {
            return "Element became stale. The page may have refreshed or changed.";
        }
        if (e instanceof WebDriverException) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("session")) {
                return "App session was lost. The app may have crashed or been terminated.";
            }
            if (msg.contains("network")) {
                return "Network connectivity issue. Check device connection.";
            }
        }
        return "An unexpected error occurred during test execution.";
    }

    /**
     * Log device and app state for debugging.
     */
    public static void logCurrentState(String context) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver != null) {
                LogManager.info("Current state at '" + context + "':");
                LogManager.info("  - Page source length: " + driver.getPageSource().length() + " characters");
                LogManager.info("  - Current activity: " + getCurrentActivity(driver));
                LogManager.info("  - Network connection: " + getNetworkInfo(driver));
            } else {
                LogManager.warn("Driver is null at '" + context + "'");
            }
        } catch (Exception e) {
            LogManager.warn("Failed to log current state: " + e.getMessage());
        }
    }

    private static String getCurrentActivity(AppiumDriver driver) {
        try {
            // This is Android-specific, would need platform detection for iOS
            if (driver instanceof io.appium.java_client.android.AndroidDriver) {
                return ((io.appium.java_client.android.AndroidDriver) driver).currentActivity();
            }
            return "N/A (iOS or unknown platform)";
        } catch (Exception e) {
            return "Unknown (" + e.getMessage() + ")";
        }
    }

    private static String getNetworkInfo(AppiumDriver driver) {
        try {
            if (driver instanceof io.appium.java_client.android.AndroidDriver) {
                var connection = ((io.appium.java_client.android.AndroidDriver) driver).getConnection();
                return connection.toString();
            }
            return "N/A (iOS or unknown platform)";
        } catch (Exception e) {
            return "Unknown (" + e.getMessage() + ")";
        }
    }
}