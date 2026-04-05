package com.tests.negative;

import com.framework.core.DriverManager;
import com.tests.BaseTest;
import com.tests.TestDataConstants;
import com.tests.pages.LoginPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * LoginNegativeTest — Error handling, boundary values, and security probes.
 */
@Epic("Authentication")
@Feature("Login — Error Handling")
public class LoginNegativeTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void setUpPage() {
        loginPage = new LoginPage(DriverManager.getDriver());
    }

    @Test(groups = "negative", description = "Wrong password displays error message")
    @Story("Invalid password shows inline error")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-N01")
    public void testInvalidPassword() {
        loginPage.login(TestDataConstants.VALID_USERNAME, TestDataConstants.INVALID_PASSWORD);

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "Error message should appear for wrong password");
        Assert.assertEquals(loginPage.getErrorMessage(),
            "Invalid email or password");
    }

    @Test(groups = "negative", description = "Empty fields keep login button disabled")
    @Story("Empty fields prevent form submission")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-N02")
    public void testEmptyFieldsDisableLoginButton() {
        Assert.assertFalse(loginPage.isLoginButtonEnabled(),
            "Login button should be disabled when both fields are empty");
    }

    @Test(groups = "negative", description = "Malformed email triggers inline validation")
    @Story("Invalid email format is rejected with validation message")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-N03")
    public void testInvalidEmailFormat() {
        loginPage.login("not-an-email", "AnyPassword1!");

        Assert.assertTrue(loginPage.isErrorDisplayed());
        Assert.assertTrue(loginPage.getErrorMessage().toLowerCase().contains("valid email"),
            "Error should mention valid email format");
    }

    @Test(groups = "negative", description = "Non-existent account shows error")
    @Story("Unregistered user cannot log in")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-N04")
    public void testNonExistentAccount() {
        loginPage.login(TestDataConstants.NONEXISTENT_USERNAME, TestDataConstants.NONEXISTENT_PASSWORD);

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "Error should appear for non-existent account");
    }

    @Test(groups = "negative", description = "SQL injection input is safely rejected")
    @Story("SQL injection does not authenticate user")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-N05")
    public void testSqlInjectionRejected() {
        loginPage.login(TestDataConstants.SQL_INJECTION_PAYLOAD, TestDataConstants.SQL_INJECTION_PAYLOAD);

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "SQL injection attempt should not authenticate");
    }

    @Test(groups = "negative", description = "512-char input is handled gracefully")
    @Story("Excessively long input does not crash the app")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-N06")
    public void testBoundaryMaxLengthInput() {
        loginPage.login(TestDataConstants.LONG_INPUT, "AnyPass1!");

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "512-char input should be rejected gracefully");
    }

    @Test(groups = "negative", description = "XSS input is not executed")
    @Story("Script injection in login field does not execute")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-N07")
    public void testXssInputRejected() {
        loginPage.login(TestDataConstants.XSS_PAYLOAD, "AnyPass1!");

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "XSS payload should be rejected, not executed");
    }
}
