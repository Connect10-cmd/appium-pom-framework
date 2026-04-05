# Appium POM Framework — Java + TestNG

Industry-standard mobile test automation framework using Appium 2, Java 17, and TestNG.  
Supports Android (UiAutomator2) and iOS (XCUITest) with full parallel execution.

---

## Quick Start

```bash
# 1. Install Appium 2 and required drivers
npm install -g appium@2
appium driver install uiautomator2
appium driver install xcuitest

# 2. Update src/main/resources/config.yaml with your device details

# 3. Start Appium server
appium

# 4. Run smoke suite
mvn test -Dplatform=android -DsuiteFile=src/test/resources/testng-suite.xml

# 5. Generate and open Allure report
mvn allure:report
open target/allure-report/index.html
```

---

## Project Structure

```
appium-pom-framework/
├── src/
│   ├── main/
│   │   ├── java/com/framework/
│   │   │   ├── core/
│   │   │   │   ├── DriverManager.java        # ThreadLocal<AppiumDriver> singleton
│   │   │   │   ├── CapabilitiesManager.java  # YAML config → Appium options
│   │   │   │   ├── BasePage.java             # W3C Actions gestures
│   │   │   │   ├── WaitUtils.java            # Centralized waits — zero Thread.sleep()
│   │   │   │   └── AppManager.java           # App lifecycle management
│   │   │   └── utils/
│   │   │       ├── LogManager.java           # Log4j2 with ThreadContext
│   │   │       ├── JsonDataReader.java       # @DataProvider from JSON
│   │   │       └── ScreenshotListener.java   # Auto-capture on failure
│   │   └── resources/
│   │       └── config.yaml                   # Device + environment config
│   └── test/
│       ├── java/com/tests/
│       │   ├── BaseTest.java                 # @BeforeMethod/@AfterMethod
│       │   ├── RetryAnalyzer.java            # Configurable retry on failure
│       │   ├── pages/
│       │   │   ├── LoginPage.java            # @AndroidFindBy + @iOSXCUITFindBy
│       │   │   └── HomePage.java
│       │   ├── positive/
│       │   │   └── LoginPositiveTest.java    # Happy path + data-driven
│       │   ├── negative/
│       │   │   └── LoginNegativeTest.java    # Error handling + security probes
│       │   └── edge/
│       │       └── EdgeCaseTest.java         # Network, rotation, kill/restart
│       └── resources/
│           ├── testng-suite.xml              # Parallel execution config
│           └── data/
│               └── login_data.json           # Test data
├── .github/workflows/
│   └── appium-ci.yml                         # GitHub Actions CI pipeline
├── Jenkinsfile                               # Jenkins pipeline
├── allure.properties                         # Allure report config
└── pom.xml                                   # Maven dependencies + plugins
```

---

## Key Design Decisions

| Concern | Solution |
|---|---|
| Parallel thread safety | `ThreadLocal<AppiumDriver>` — each thread owns its driver |
| Zero flakiness | All waits via `WaitUtils` — `Thread.sleep()` is banned |
| Fast test isolation | `AppManager.resetApp()` = terminate + relaunch (~5x faster than fullReset) |
| Cross-platform locators | `@AndroidFindBy` + `@iOSXCUITFindBy` on every element |
| Auto failure capture | `ScreenshotListener` attaches PNG to Allure on any failure |
| Data-driven testing | JSON files + `@DataProvider(parallel=true)` |
| Flaky test recovery | `RetryAnalyzer` retries up to 2 times before marking failed |

---

## Running Specific Suites

```bash
# Smoke only (fastest — runs on every commit)
mvn test -Dplatform=android -DsuiteFile=src/test/resources/testng-suite.xml -Dgroups=smoke

# Negative tests only
mvn test -Dplatform=android -DsuiteFile=src/test/resources/testng-suite.xml -Dgroups=negative

# iOS
mvn test -Dplatform=ios -DsuiteFile=src/test/resources/testng-suite.xml

# Parallel on 3 threads (default in testng-suite.xml)
mvn test -DsuiteFile=src/test/resources/testng-suite.xml
```

---

## CI/CD

- **GitHub Actions**: `.github/workflows/appium-ci.yml`
  - Triggers on push, pull request, and nightly (2AM UTC)
  - Spins up Android emulator with KVM acceleration
  - Publishes Allure report as a downloadable artifact

- **Jenkins**: `Jenkinsfile`
  - Parameterized: platform, suite file, device name
  - Publishes Allure report + HTML report
  - Sends email on failure

---

## Adding a New Page Object

1. Create `src/test/java/com/tests/pages/YourPage.java`
2. Extend `BasePage`
3. Add `@AndroidFindBy` + `@iOSXCUITFindBy` locators
4. Call `PageFactory.initElements(new AppiumFieldDecorator(driver), this)` in constructor
5. Add `@Step` annotations to all public actions

## Adding a New Test

1. Create in the appropriate package (`positive/`, `negative/`, or `edge/`)
2. Extend `BaseTest`
3. Annotate with `@Epic`, `@Feature`, `@Story`, `@Severity`, `@TmsLink`
4. Add `@Test(groups = "smoke")` for critical paths

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| io.appium:java-client | 9.2.3 | Appium driver + PageFactory |
| org.testng:testng | 7.10.2 | Test runner + parallel execution |
| io.qameta.allure:allure-testng | 2.27.0 | Rich HTML reporting |
| org.apache.logging.log4j:log4j-core | 2.23.1 | Thread-aware logging |
| org.yaml:snakeyaml | 2.2 | YAML config loading |
| com.fasterxml.jackson.core:jackson-databind | 2.17.1 | JSON data reading |


## Author

Sachin Tiwari

This framework was designed and built to solve real-world mobile automation challenges with focus on stability and maintainability.
