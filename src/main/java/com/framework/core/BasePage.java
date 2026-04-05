package com.framework.core;

import com.framework.utils.ActionWrapper;
import com.framework.utils.ExceptionHandler;
import com.framework.utils.LogManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import java.time.Duration;
import java.util.List;

/**
 * BasePage — Abstract base for all Page Objects.
 * All gestures use the W3C Actions API (TouchAction is deprecated in Appium 2+).
 * All waits delegate to WaitUtils — zero Thread.sleep() in this class.
 */
public abstract class BasePage {

    protected AppiumDriver driver;
    protected WaitUtils wait;
    protected ActionWrapper actions;

    public BasePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.actions = new ActionWrapper(driver, wait);
    }

    // ── Tap ─────────────────────────────────────────────────────────────────

    protected void tap(WebElement element) {
        wait.waitForClickable(element).click();
    }

    protected void tapByCoords(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), x, y))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(tap));
    }

    // ── Swipe (W3C Actions API) ──────────────────────────────────────────────

    protected void swipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(Duration.ofMillis(600),
                PointerInput.Origin.viewport(), endX, endY))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }

    protected void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width / 2, size.height * 3 / 4, size.width / 2, size.height / 4);
    }

    protected void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width / 2, size.height / 4, size.width / 2, size.height * 3 / 4);
    }

    protected void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width * 3 / 4, size.height / 2, size.width / 4, size.height / 2);
    }

    protected void swipeRight() {
        Dimension size = driver.manage().window().getSize();
        swipe(size.width / 4, size.height / 2, size.width * 3 / 4, size.height / 2);
    }

    // ── Long Press ────────────────────────────────────────────────────────────

    protected void longPress(WebElement element) {
        Point loc = element.getLocation();
        Dimension size = element.getSize();
        int cx = loc.x + size.width / 2;
        int cy = loc.y + size.height / 2;
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence press = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), cx, cy))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(Duration.ofMillis(1500),
                PointerInput.Origin.viewport(), cx, cy))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(press));
    }

    // ── Double Tap ─────────────────────────────────────────────────────────

    protected void doubleTap(WebElement element) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Point loc = wait.waitForClickable(element).getLocation();
        Dimension size = element.getSize();
        int cx = loc.x + size.width / 2;
        int cy = loc.y + size.height / 2;
        Sequence doubleTap = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), cx, cy))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(Duration.ofMillis(100),
                PointerInput.Origin.viewport(), cx, cy))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(doubleTap));
    }

    // ── Scroll to Element ──────────────────────────────────────────────────

    protected WebElement scrollToElement(String text) {
        return driver.findElement(AppiumBy.androidUIAutomator(
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
    }

    // ── Input ──────────────────────────────────────────────────────────────

    protected void typeText(WebElement element, String text) {
        wait.waitForClickable(element).clear();
        element.sendKeys(text);
    }

    protected String getText(WebElement element) {
        return wait.waitForVisible(element).getText();
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected void hideKeyboard() {
        try {
            if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).hideKeyboard();
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).hideKeyboard();
            }
        } catch (Exception ignored) { /* keyboard may not be visible */ }
    }
}
