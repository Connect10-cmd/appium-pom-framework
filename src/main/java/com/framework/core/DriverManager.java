package com.framework.core;

import com.framework.utils.LogManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;

/**
 * DriverManager — Singleton pattern with ThreadLocal for parallel test safety.
 * Each thread (device) gets its own isolated AppiumDriver instance.
 */
public class DriverManager {

    // ThreadLocal ensures each parallel thread gets its own driver instance
    private static final ThreadLocal<AppiumDriver> driverThread = new ThreadLocal<>();

    private DriverManager() { /* Prevent instantiation */ }

    public static AppiumDriver getDriver() {
        return driverThread.get();
    }

    public static void initDriver(String platform) {
        CapabilitiesManager caps = new CapabilitiesManager();
        URL serverUrl;
        try {
            serverUrl = caps.getAppiumServerUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Appium server URL", e);
        }

        AppiumDriver driver;
        switch (platform.toLowerCase()) {
            case "android":
                driver = new AndroidDriver(serverUrl, caps.getAndroidOptions());
                break;
            case "ios":
                driver = new IOSDriver(serverUrl, caps.getIOSOptions());
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }

        driverThread.set(driver);
        LogManager.info("Driver initialized for: " + platform
            + " [Thread: " + Thread.currentThread().getName() + "]");
    }

    public static void quitDriver() {
        if (driverThread.get() != null) {
            driverThread.get().quit();
            driverThread.remove(); // Critical: prevents memory leaks in parallel runs
            LogManager.info("Driver quit and removed from ThreadLocal.");
        }
    }
}
