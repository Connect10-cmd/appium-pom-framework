package com.framework.utils;

import com.framework.core.WaitUtils;
import com.framework.utils.LogManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

/**
 * AndroidActions — Platform-specific actions for Android.
 * Extends MobileActions with Android-optimized gestures.
 */
public class AndroidActions extends MobileActions {
    private final AndroidDriver androidDriver;

    public AndroidActions(AppiumDriver driver, WaitUtils wait) {
        super(driver, wait);
        this.androidDriver = (AndroidDriver) driver;
    }

    @Override
    public void scrollToElement(String text, String context) {
        executeWithRetry(() -> {
            LogManager.debug("Android scroll to '" + text + "' in " + context);
            androidDriver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))" +
                ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
            LogManager.debug("Successfully scrolled to '" + text + "' on Android");
        }, "Android scroll to " + text);
    }

    @Override
    public void swipe(String direction, String context) {
        executeWithRetry(() -> {
            LogManager.debug("Android swipe " + direction + " in " + context);
            // Android-specific swipe implementation using PointerInput
            // Implement based on direction
            super.swipe(direction, context); // Fallback to base if needed
        }, "Android swipe " + direction);
    }
}
