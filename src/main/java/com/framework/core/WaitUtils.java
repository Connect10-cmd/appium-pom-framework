package com.framework.core;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

/**
 * WaitUtils — Centralized wait strategy.
 * All page interactions route through here — zero Thread.sleep() in test code.
 * Uses WebDriverWait for standard waits and FluentWait for polling-based conditions.
 */
public class WaitUtils {

    private static final int DEFAULT_TIMEOUT  = 15;  // seconds
    private static final int POLLING_INTERVAL = 500; // milliseconds

    private final WebDriverWait wait;
    private final FluentWait<WebDriver> fluentWait;

    public WaitUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.fluentWait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
            .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class);
    }

    /** Wait for an element to be visible on screen. */
    public WebElement waitForVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /** Wait for an element to be visible and interactable. */
    public WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /** Wait for an element to disappear (e.g. loading spinners). */
    public boolean waitForInvisible(WebElement element) {
        return wait.until(ExpectedConditions.invisibilityOf(element));
    }

    /** Wait for specific text to appear in an element. */
    public boolean waitForText(WebElement element, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    /** Wait for a locator to match a clickable element (useful post-navigation). */
    public WebElement waitForLocator(By locator) {
        return fluentWait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Generic fluent wait for any custom ExpectedCondition. */
    public <T> T waitFor(ExpectedCondition<T> condition) {
        return fluentWait.until(condition);
    }
}
