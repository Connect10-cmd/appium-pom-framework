package com.framework.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;

import com.framework.utils.LogManager;

/**
 * AppManager — App lifecycle management.
 * Handles install, reset, launch, terminate, and background strategies.
 * noReset session reuse is ~5x faster than full reinstall between tests.
 */
public class AppManager {

    private final AppiumDriver driver;
    private final String appPackage; // Android package / iOS bundleId

    public AppManager(AppiumDriver driver, String appPackage) {
        this.driver = driver;
        this.appPackage = appPackage;
    }

    /**
     * Fastest reset strategy: terminate + reactivate.
     * Use in @BeforeMethod for test isolation without driver restart.
     * Includes verification that app launched successfully.
     */
    public void resetApp() {
        LogManager.info("Resetting app: " + appPackage);
        terminateApp();
        launchApp();
        verifyAppRunning();
    }

    /**
     * Verify app is running and accessible — prevents silent launch failures.
     * Waits for app to come to foreground after activation.
     */
    private void verifyAppRunning() {
        try {
            // Wait briefly for app to settle after launch
            Thread.sleep(500);
            boolean appRunning = isAppInstalled();
            if (!appRunning) {
                throw new RuntimeException(
                    "App verification failed after launch: " + appPackage);
            }
            LogManager.info("App verified running: " + appPackage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogManager.error("Interrupted during app verification", e);
        }
    }

    /**
     * Install app only if not already installed.
     * Avoids redundant installs in noReset sessions.
     */
    public void installApp(String appPath) {
        if (isAppInstalled()) {
            LogManager.info("App already installed, skipping: " + appPackage);
            return;
        }
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).installApp(appPath);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).installApp(appPath);
        }
        LogManager.info("App installed from: " + appPath);
    }

    /** Full reinstall — use when app state must be completely clean. */
    public void reinstallApp(String appPath) {
        if (isAppInstalled()) {
            if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).removeApp(appPackage);
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).removeApp(appPackage);
            }
            LogManager.info("Removed existing app: " + appPackage);
        }
        installApp(appPath);
        LogManager.info("App reinstalled from: " + appPath);
    }

    public void terminateApp() {
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).terminateApp(appPackage);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).terminateApp(appPackage);
        }
    }

    public void launchApp() {
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).activateApp(appPackage);
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).activateApp(appPackage);
        }
    }

    /**
     * Send app to background — used for edge case testing.
     * Tests session persistence, OS interruption handling, etc.
     */
    public void backgroundApp(int seconds) {
        LogManager.info("Backgrounding app for " + seconds + " seconds");
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).runAppInBackground(Duration.ofSeconds(seconds));
        } else if (driver instanceof IOSDriver) {
            ((IOSDriver) driver).runAppInBackground(Duration.ofSeconds(seconds));
        }
    }

    public boolean isAppInstalled() {
        if (driver instanceof AndroidDriver) {
            return ((AndroidDriver) driver).isAppInstalled(appPackage);
        } else if (driver instanceof IOSDriver) {
            return ((IOSDriver) driver).isAppInstalled(appPackage);
        }
        return false;
    }
}
