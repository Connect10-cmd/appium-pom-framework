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
<<<<<<< HEAD
    private long testStartTime;
=======
>>>>>>> 2a1f502 (feat: implement core framework structure with POM, driver management, actions, and config support)

    @Parameters({"platform"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("android") String platform) {
<<<<<<< HEAD
        testStartTime = System.currentTimeMillis();
=======
>>>>>>> 2a1f502 (feat: implement core framework structure with POM, driver management, actions, and config support)
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
<<<<<<< HEAD
        long duration = System.currentTimeMillis() - testStartTime;
        LogManager.info("=== Tearing down driver ===");
        DriverManager.quitDriver();
        if (duration > 30000) { // 30 seconds
            LogManager.warn("SLOW TEST DETECTED: " + duration + "ms (>30s)");
        } else {
            LogManager.info("Test completed in " + duration + "ms");
        }
=======
        LogManager.info("=== Tearing down driver ===");
        DriverManager.quitDriver();
>>>>>>> 2a1f502 (feat: implement core framework structure with POM, driver management, actions, and config support)
    }
}
