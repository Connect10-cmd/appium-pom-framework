package com.framework.core;

import com.framework.utils.LogManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DriverManager — Enhanced singleton pattern with ThreadLocal for parallel test safety.
 * Supports multiple devices per platform with device-specific capabilities.
 * Each thread (device) gets its own isolated AppiumDriver instance.
 */
public class DriverManager {

    // ThreadLocal ensures each parallel thread gets its own driver instance
    private static final ThreadLocal<AppiumDriver> driverThread = new ThreadLocal<>();

    // Track active drivers for cleanup
    private static final Map<String, AppiumDriver> activeDrivers = new ConcurrentHashMap<>();

    private DriverManager() { /* Prevent instantiation */ }

    public static AppiumDriver getDriver() {
        return driverThread.get();
    }

    /**
     * Initialize driver for specific platform and device.
     * @param platform android/ios
     * @param deviceName optional device identifier for multi-device support
     */
    public static void initDriver(String platform, String deviceName) {
        CapabilitiesManager caps = CapabilitiesManager.getInstance();
        URL serverUrl;
        try {
            serverUrl = caps.getAppiumServerUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Appium server URL", e);
        }

        AppiumDriver driver;
        String threadKey = Thread.currentThread().getName() +
            (deviceName != null ? "-" + deviceName : "");

        switch (platform.toLowerCase()) {
            case "android":
                UiAutomator2Options androidCaps = caps.getAndroidOptions();
                if (deviceName != null) {
                    // Override device name if specified
                    androidCaps.setDeviceName(deviceName);
                }
                driver = new AndroidDriver(serverUrl, androidCaps);
                break;
            case "ios":
                XCUITestOptions iosCaps = caps.getIOSOptions();
                if (deviceName != null) {
                    // Override device name if specified
                    iosCaps.setDeviceName(deviceName);
                }
                driver = new IOSDriver(serverUrl, iosCaps);
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }

        driverThread.set(driver);
        activeDrivers.put(threadKey, driver);

        LogManager.info("Driver initialized for: " + platform +
            (deviceName != null ? " (device: " + deviceName + ")" : "") +
            " [Thread: " + threadKey + "]");
    }

    /**
     * Initialize driver for platform only (backward compatibility).
     */
    public static void initDriver(String platform) {
        initDriver(platform, null);
    }

    /**
     * Quit driver for current thread and clean up resources.
     */
    public static void quitDriver() {
        quitDriver(null);
    }

    public static void quitDriver(String deviceName) {
        AppiumDriver driver = driverThread.get();
        if (driver != null) {
            String threadKey = Thread.currentThread().getName() +
                (deviceName != null ? "-" + deviceName : "");
            try {
                driver.quit();
                LogManager.info("Driver quit successfully [Thread: " + threadKey + "]");
            } catch (Exception e) {
                LogManager.warn("Error while quitting driver [Thread: " + threadKey + "]: " + e.getMessage());
            } finally {
                driverThread.remove();
                activeDrivers.remove(threadKey);
            }
        }
    }

    /**
     * Quit all active drivers (useful for cleanup in test suites).
     */
    public static void quitAllDrivers() {
        LogManager.info("Quitting all active drivers...");
        activeDrivers.forEach((threadKey, driver) -> {
            try {
                driver.quit();
                LogManager.info("Driver quit for thread: " + threadKey);
            } catch (Exception e) {
                LogManager.warn("Error quitting driver for thread " + threadKey + ": " + e.getMessage());
            }
        });
        activeDrivers.clear();
        driverThread.remove(); // Clean current thread
        LogManager.info("All drivers quit and cleaned up");
    }

    /**
     * Get count of active drivers (useful for monitoring).
     */
    public static int getActiveDriverCount() {
        return activeDrivers.size();
    }

    /**
     * Check if current thread has an active driver.
     */
    public static boolean isAppHealthy() {
        AppiumDriver driver = getDriver();
        if (driver == null) return false;
        if (driver.getSessionId() == null) return false;
        try {
            // Check if session is active by getting page source
            driver.getPageSource();
            return true;
        } catch (Exception e) {
            LogManager.warn("App health check failed: " + e.getMessage());
            return false;
        }
    }

    public static void restartDriverIfNeeded(String platform, String deviceName) {
        if (!isAppHealthy()) {
            LogManager.info("App unhealthy, restarting driver for " + platform + " device: " + deviceName);
            quitDriver(deviceName);
            initDriver(platform, deviceName);
        }
    }
}
