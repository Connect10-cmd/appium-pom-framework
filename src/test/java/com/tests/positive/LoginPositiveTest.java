package com.tests.positive;

import com.framework.core.DriverManager;
import com.framework.utils.JsonDataReader;
import com.tests.BaseTest;
import com.tests.TestDataConstants;
import com.tests.pages.HomePage;
import com.tests.pages.LoginPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.Map;

/**
 * LoginPositiveTest — Happy path and data-driven positive test cases.
 */
@Epic("Authentication")
@Feature("Login — Positive")
public class LoginPositiveTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void setUpPage() {
        loginPage = new LoginPage(DriverManager.getDriver());
    }

    @DataProvider(name = "validCredentials", parallel = true)
    public Object[][] validCredentials() throws Exception {
        return JsonDataReader.readData(
            "src/test/resources/data/login_data.json");
    }

    @Test(groups = {"smoke", "positive"}, description = "Valid credentials navigate to home screen")
    @Story("User can log in with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @TmsLink("TC-P01")
    public void testValidLogin() {
        loginPage.login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);

        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertTrue(home.isHomePageDisplayed(),
            "Home page should be displayed after valid login");
    }

    @Test(dataProvider = "validCredentials", groups = "positive",
          description = "Data-driven valid login across multiple accounts")
    @Story("Multiple valid users can successfully log in")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-P02")
    public void testValidLoginDataDriven(Map<String, String> data) {
        if (!"success".equals(data.get("expectedResult"))) return;

        loginPage.login(data.get("username"), data.get("password"));

        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertTrue(home.isHomePageDisplayed(),
            "Home page should display for user: " + data.get("username"));
    }

    @Test(groups = "positive", description = "Session persists after app is backgrounded")
    @Story("Session survives OS background interruption")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-P03")
    public void testSessionPersistenceAfterBackground() throws Exception {
        loginPage.login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);
        appManager.backgroundApp(3);

        HomePage home = new HomePage(DriverManager.getDriver());
        Assert.assertTrue(home.isHomePageDisplayed(),
            "Session should survive a 3-second background");
    }

    @Test(groups = "positive", description = "User can logout and login again")
    @Story("Logout and re-login works correctly")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("TC-P04")
    public void testLogoutAndReLogin() {
        loginPage.login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);

        HomePage home = new HomePage(DriverManager.getDriver());
        LoginPage loggedOutPage = home.logout();

        Assert.assertTrue(loggedOutPage.isLoginPageDisplayed(),
            "Login page should appear after logout");

        loggedOutPage.login(TestDataConstants.VALID_USERNAME, TestDataConstants.VALID_PASSWORD);
        Assert.assertTrue(new HomePage(DriverManager.getDriver()).isHomePageDisplayed(),
            "User should be able to re-login after logout");
    }
}
