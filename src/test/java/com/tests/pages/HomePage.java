package com.tests.pages;

import com.framework.core.BasePage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * HomePage — Page Object for the home/dashboard screen (post-login).
 */
public class HomePage extends BasePage {

    @AndroidFindBy(accessibility = "home_title")
    @iOSXCUITFindBy(accessibility = "home_title")
    private WebElement homeTitle;

    @AndroidFindBy(accessibility = "user_avatar")
    @iOSXCUITFindBy(accessibility = "user_avatar")
    private WebElement userAvatar;

    @AndroidFindBy(id = "com.example.app:id/logout_btn")
    @iOSXCUITFindBy(accessibility = "logout_button")
    private WebElement logoutButton;

    @AndroidFindBy(id = "com.example.app:id/menu_button")
    @iOSXCUITFindBy(accessibility = "menu_button")
    private WebElement menuButton;

    public HomePage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public boolean isHomePageDisplayed() {
        return isDisplayed(homeTitle);
    }

    @Step("Get home page title text")
    public String getTitle() {
        return getText(homeTitle);
    }

    @Step("Tap logout and return to Login page")
    public LoginPage logout() {
        tap(logoutButton);
        return new LoginPage(driver);
    }

    @Step("Open navigation menu")
    public void openMenu() {
        tap(menuButton);
    }
}
