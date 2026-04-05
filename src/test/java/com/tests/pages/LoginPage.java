package com.tests.pages;

import com.framework.core.BasePage;
import com.framework.utils.LogManager;
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
    }

    // ── Page Actions ─────────────────────────────────────────────────────────

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        typeText(usernameField, username);
        LogManager.info("Entered username field");
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        typeText(passwordField, password);
        return this;
    }

    @Step("Tap Login button")
    public LoginPage tapLogin() {
        tap(loginButton);
        wait.waitForInvisible(loadingSpinner);
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
        tap(forgotPasswordLink);
    }

    // ── Getters for Assertions ───────────────────────────────────────────────

    @Step("Get error message text")
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public boolean isLoginButtonEnabled() {
        return loginButton.isEnabled();
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(usernameField);
    }
}
