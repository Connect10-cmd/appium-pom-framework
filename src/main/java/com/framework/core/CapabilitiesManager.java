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

    private static CapabilitiesManager instance;
    private final Map<String, Object> config;

    @SuppressWarnings("unchecked") CapabilitiesManager() {
        Yaml yaml = new Yaml();
        String env = System.getProperty("env", System.getenv().getOrDefault("ENV", "dev"));
        String configFile = "/config-" + env + ".yaml";
        InputStream in = getClass().getResourceAsStream(configFile);
        if (in == null) {
            // Fallback to default config.yaml
            in = getClass().getResourceAsStream("/config.yaml");
            if (in == null) {
                throw new RuntimeException("Config file not found: " + configFile + " or config.yaml");
            }
        }
        config = yaml.load(in);
    }

    public static CapabilitiesManager getInstance() {
        if (instance == null) {
            instance = new CapabilitiesManager();
        }
        return instance;
    }

    public URL getAppiumServerUrl() throws Exception {
        String serverUrl = System.getProperty("appium.server.url",
            System.getenv().getOrDefault("APPIUM_SERVER_URL",
                (String) config.get("appiumServer")));
        return new URL(serverUrl);
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> getRetryConfig() {
        return (Map<String, Object>) config.get("retry");
    }

    public int getMaxRetryAttempts() {
        Map<String, Object> retry = getRetryConfig();
        return (Integer) retry.getOrDefault("maxAttempts", 3);
    }

    public long getRetryDelayMs() {
        Map<String, Object> retry = getRetryConfig();
        return (Integer) retry.getOrDefault("retryDelayMs", 500);
    }

    public boolean isHighlightingEnabled() {
        Map<String, Object> retry = getRetryConfig();
        return (Boolean) retry.getOrDefault("enableHighlighting", true);
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getRetryExceptions() {
        Map<String, Object> retry = getRetryConfig();
        return (java.util.List<String>) retry.getOrDefault("exceptions",
            java.util.Arrays.asList("StaleElementReferenceException", "NoSuchElementException"));
    }

    public Duration getDefaultTimeout() {
        Map<String, Object> retry = getRetryConfig();
        Integer timeoutSeconds = (Integer) retry.getOrDefault("defaultTimeout", 10);
        return Duration.ofSeconds(timeoutSeconds);
    }
}
