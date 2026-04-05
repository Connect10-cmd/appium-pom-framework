package com.tests;

import com.framework.core.AppManager;
import com.framework.core.DriverManager;
import com.framework.utils.LogManager;
import org.testng.annotations.*;

/**
 * BaseTest — Thread-safe setup and teardown for all test classes.
 * All test classes extend this. Driver is initialized per-method for isolation.
 */
public abstract class BaseTest {

    protected AppManager appManager;
    private long testStartTime;

    @Parameters({"platform"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("android") String platform) {
        testStartTime = System.currentTimeMillis();
        LogManager.info("=== Setting up driver for platform: " + platform + " ===");
        DriverManager.initDriver(platform);
        appManager = new AppManager(
            DriverManager.getDriver(),
            "com.example.myapp");
        appManager.resetApp();
        LogManager.info("App reset complete. Ready for test.");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        long duration = System.currentTimeMillis() - testStartTime;
        LogManager.info("=== Tearing down driver ===");
        DriverManager.quitDriver();
        if (duration > 30000) { // 30 seconds
            LogManager.warn("SLOW TEST DETECTED: " + duration + "ms (>30s)");
        } else {
            LogManager.info("Test completed in " + duration + "ms");
        }
    }
}
