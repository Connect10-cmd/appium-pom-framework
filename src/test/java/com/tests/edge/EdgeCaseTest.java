package com.tests.edge;

import com.framework.core.DriverManager;
import com.tests.BaseTest;
import com.tests.TestDataConstants;
import com.tests.pages.HomePage;
import com.tests.pages.LoginPage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import io.qameta.allure.*;
import org.openqa.selenium.ScreenOrientation;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * EdgeCaseTest — Device interruptions, network failures, orientation changes.
 */
@Epic("Resilience")
@Feature("Edge Cases")
public class EdgeCaseTest extends BaseTest {

    @Test(groups = "edge", description = "Offline login shows network error message")
    @Story("Login attempt while offline shows helpful error")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-E01")
    public void testLoginWhileOffline() {
        AndroidDriver aDriver = (AndroidDriver) DriverManager.getDriver();

        // Disable all network connections
        try {
            aDriver.setConnection(new ConnectionStateBuilder()
                .withAirplaneModeEnabled()
                .build());

            new LoginPage(aDriver).login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);

            LoginPage lp = new LoginPage(aDriver);
            Assert.assertTrue(lp.isErrorDisplayed(),
                "Error should appear when login attempted offline");
            Assert.assertTrue(lp.getErrorMessage().toLowerCase().contains("network"),
                "Error message should reference network connectivity");
        } finally {
            // Always restore network — never leave device offline after test
            aDriver.setConnection(new ConnectionStateBuilder()
                .withAirplaneModeDisabled()
                .build());
        }
    }

    @Test(groups = "edge", description = "Username persists through screen rotation")
    @Story("App survives screen rotation during login entry")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-E02")
    public void testLoginSurvivesScreenRotation() {
        LoginPage lp = new LoginPage(DriverManager.getDriver());
        lp.enterUsername(TestDataConstants.VALID_USERNAME);

        // Rotation is Android-specific; skip for iOS
        AppiumDriver driver = DriverManager.getDriver();
        if (driver instanceof AndroidDriver) {
            ((AndroidDriver) driver).rotate(ScreenOrientation.LANDSCAPE);
            ((AndroidDriver) driver).rotate(ScreenOrientation.PORTRAIT);
        }

        lp = new LoginPage(driver);
        lp.enterPassword(TestDataConstants.VALID_PASSWORD).tapLogin();

        Assert.assertTrue(
            new HomePage(driver).isHomePageDisplayed(),
            "Login should succeed after rotation");
    }

    @Test(groups = "edge", description = "App returns to expected state after kill and relaunch")
    @Story("Force-killed app resumes cleanly")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-E03")
    public void testAppKillAndRestart() {
        appManager.terminateApp();
        appManager.launchApp();

        LoginPage lp = new LoginPage(DriverManager.getDriver());
        Assert.assertTrue(lp.isLoginPageDisplayed(),
            "Login page should be displayed after kill and relaunch");
    }

    @Test(groups = "edge", description = "App recovers correctly from 30s background")
    @Story("Extended background does not break app state")
    @Severity(SeverityLevel.MINOR)
    @TmsLink("TC-E04")
    public void testExtendedBackground() throws Exception {
        new LoginPage(DriverManager.getDriver()).login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);
        appManager.backgroundApp(30);

        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertTrue(home.isHomePageDisplayed(),
            "Home page should still be displayed after 30s background");
    }
}
