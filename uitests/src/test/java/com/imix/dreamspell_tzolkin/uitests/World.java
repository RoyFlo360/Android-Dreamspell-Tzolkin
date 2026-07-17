package com.imix.dreamspell_tzolkin.uitests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

/**
 * Shared Appium session + element helpers for the BDD steps. One AndroidDriver per scenario,
 * created in {@link Hooks}. Element lookup prefers stable resource-ids, with visible text as a
 * fallback (both are stable in this app's layouts).
 */
public class World {

    public static final String PKG = "com.imix.dreamspell_tzolkin";
    private static final String SERVER = System.getProperty("appium.server", "http://127.0.0.1:4723/");
    private static final String DEVICE = System.getProperty("appium.device", "Android");

    public AndroidDriver driver;
    private WebDriverWait wait;

    public void start() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(DEVICE)
                .setAutomationName("UiAutomator2")
                .setAppPackage(PKG)
                .setAppActivity(".OracleActivity")
                .setNoReset(true)          // keep language/prefs; we're driving the installed build
                .setNewCommandTimeout(Duration.ofSeconds(120));
        driver = new AndroidDriver(new URL(SERVER), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void stop() {
        if (driver != null) driver.quit();
    }

    // ---- element lookup ----

    public By id(String resId) {
        return By.id(PKG + ":id/" + resId);
    }

    /** Exact visible text (labels, menu rows, chips). */
    public By text(String value) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + value + "\")");
    }

    /** Substring of visible text (e.g. the "Combined · …" result caption, "Kin 214: …"). */
    public By textContains(String value) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + value + "\")");
    }

    public WebElement waitFor(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void tap(By by) {
        waitFor(by).click();
    }

    public boolean isPresent(By by) {
        List<WebElement> els = driver.findElements(by);
        return !els.isEmpty() && els.get(0).isDisplayed();
    }

    public String textOf(By by) {
        return waitFor(by).getText();
    }

    // ---- gestures ----

    /** Horizontal swipe across the pager to move between the four primary screens. */
    public void swipeLeft() {
        var size = driver.manage().window().getSize();
        int y = size.height / 2, x1 = (int) (size.width * 0.85), x2 = (int) (size.width * 0.15);
        driver.executeScript("mobile: swipeGesture", java.util.Map.of(
                "left", x2, "top", y - 5, "width", x1 - x2, "height", 10, "direction", "left", "percent", 0.9));
    }

    public void swipeRight() {
        var size = driver.manage().window().getSize();
        int y = size.height / 2, x1 = (int) (size.width * 0.15), x2 = (int) (size.width * 0.85);
        driver.executeScript("mobile: swipeGesture", java.util.Map.of(
                "left", x1, "top", y - 5, "width", x2 - x1, "height", 10, "direction", "right", "percent", 0.9));
    }

    // ---- screenshots ----

    public void screenshot(String name) {
        try {
            File dir = new File("build/appium-screenshots");
            dir.mkdirs();
            File src = driver.getScreenshotAs(OutputType.FILE);
            String safe = name.replaceAll("[^a-zA-Z0-9-_]", "_");
            Files.copy(src.toPath(), new File(dir, safe + ".png").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("screenshot failed: " + e.getMessage());
        }
    }
}
