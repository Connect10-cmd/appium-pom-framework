package com.framework.utils;

import com.framework.core.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.ByteArrayInputStream;

/**
 * ScreenshotListener — ITestListener that auto-captures screenshots on failure.
 * Screenshots are attached to the Allure report as PNG images.
 * Register in testng-suite.xml under <listeners>.
 */
public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        LogManager.setTestName(result.getName());
        LogManager.info(">>> TEST STARTED: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LogManager.info("<<< TEST PASSED: " + result.getName());
        LogManager.clearContext();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LogManager.error("<<< TEST FAILED: " + result.getName(), result.getThrowable());
        captureScreenshot(result.getName());
        LogManager.clearContext();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LogManager.warn("--- TEST SKIPPED: " + result.getName());
        LogManager.clearContext();
    }

    private void captureScreenshot(String testName) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                LogManager.warn("Driver is null; cannot capture screenshot for: " + testName);
                return;
            }

            if (!(driver instanceof TakesScreenshot)) {
                LogManager.warn("Driver does not support screenshots for: " + testName);
                return;
            }

            byte[] screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);

            Allure.addAttachment(
                "Failure Screenshot — " + testName,
                "image/png",
                new ByteArrayInputStream(screenshot),
                "png");

            LogManager.info("Screenshot captured for: " + testName);
        } catch (Exception e) {
            LogManager.error("Failed to capture screenshot for: " + testName, e);
        }
    }
}
