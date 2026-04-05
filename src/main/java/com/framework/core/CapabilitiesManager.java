package com.framework.core;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * CapabilitiesManager — Loads device configuration from config.yaml.
 * Supports UiAutomator2Options (Android) and XCUITestOptions (iOS).
 */
public class CapabilitiesManager {

    private final Map<String, Object> config;

    @SuppressWarnings("unchecked")
    public CapabilitiesManager() {
        Yaml yaml = new Yaml();
        InputStream in = getClass().getResourceAsStream("/config.yaml");
        if (in == null) {
            throw new RuntimeException("config.yaml not found in classpath resources.");
        }
        config = yaml.load(in);
    }

    public URL getAppiumServerUrl() throws Exception {
        return new URL((String) config.get("appiumServer"));
    }

    @SuppressWarnings("unchecked")
    public UiAutomator2Options getAndroidOptions() {
        Map<String, Object> android = (Map<String, Object>) config.get("android");
        return new UiAutomator2Options()
            .setDeviceName((String) android.get("deviceName"))
            .setPlatformVersion((String) android.get("platformVersion"))
            .setApp((String) android.get("appPath"))
            .setAppPackage((String) android.get("appPackage"))
            .setAppActivity((String) android.get("appActivity"))
            .setNoReset((Boolean) android.getOrDefault("noReset", true))
            .setAutoGrantPermissions(true)
            .setNewCommandTimeout(Duration.ofSeconds(60));
    }

    @SuppressWarnings("unchecked")
    public XCUITestOptions getIOSOptions() {
        Map<String, Object> ios = (Map<String, Object>) config.get("ios");
        return new XCUITestOptions()
            .setDeviceName((String) ios.get("deviceName"))
            .setPlatformVersion((String) ios.get("platformVersion"))
            .setApp((String) ios.get("appPath"))
            .setBundleId((String) ios.get("bundleId"))
            .setNoReset((Boolean) ios.getOrDefault("noReset", true))
            .setWdaLocalPort(8100);
    }
}
