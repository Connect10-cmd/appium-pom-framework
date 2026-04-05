package com.tests.pages;

import com.framework.core.BasePage;
import com.framework.utils.MobileActions;
import com.framework.utils.MobileActionsFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * LoginPage — Page Object for the login screen.
 * Uses @AndroidFindBy + @iOSXCUITFindBy for cross-platform element location.
 */
public class LoginPage extends BasePage {

    private final MobileActions actions;
    // ── Locators: dual annotation for Android + iOS ──────────────────────────

    @AndroidFindBy(accessibility = "username_field")
    @iOSXCUITFindBy(accessibility = "username_field")
    private WebElement usernameField;

    @AndroidFindBy(accessibility = "password_field")
    @iOSXCUITFindBy(accessibility = "password_field")
    private WebElement passwordField;

    @AndroidFindBy(id = "com.example.app:id/login_button")
    @iOSXCUITFindBy(accessibility = "login_button")
    private WebElement loginButton;

    @AndroidFindBy(id = "com.example.app:id/error_message")
    @iOSXCUITFindBy(accessibility = "error_label")
    private WebElement errorMessage;

    @AndroidFindBy(xpath = "//*[@text='Forgot Password?']")
    @iOSXCUITFindBy(accessibility = "forgot_password_link")
    private WebElement forgotPasswordLink;

    @AndroidFindBy(id = "com.example.app:id/loading_spinner")
    @iOSXCUITFindBy(accessibility = "loading_indicator")
    private WebElement loadingSpinner;

    public LoginPage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
        String platform = System.getProperty("platform", "android");
        this.actions = MobileActionsFactory.getActions(platform, driver, wait);
    }

    // ── Page Actions ─────────────────────────────────────────────────────────

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        actions.sendKeys(usernameField, username, "Username Field");
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        actions.sendKeys(passwordField, password, "Password Field");
        return this;
    }

    @Step("Tap Login button")
    public LoginPage tapLogin() {
        actions.click(loginButton, "Login Button");
        actions.waitForVisible(loadingSpinner, "Loading Spinner");
        return this;
    }

    @Step("Login with credentials for user: {username}")
    public LoginPage login(String username, String password) {
        return enterUsername(username)
            .enterPassword(password)
            .tapLogin();
    }

    @Step("Tap Forgot Password link")
    public void tapForgotPassword() {
        actions.click(forgotPasswordLink, "Forgot Password Link");
    }

    // ── Getters for Assertions ───────────────────────────────────────────────

    @Step("Get error message text")
    public String getErrorMessage() {
        return actions.getText(errorMessage, "Error Message");
    }

    public boolean isErrorDisplayed() {
        return actions.isDisplayed(errorMessage, "Error Message");
    }

    public boolean isLoginButtonEnabled() {
        return actions.waitForClickable(loginButton, "Login Button").isEnabled();
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(usernameField);
    }
}
