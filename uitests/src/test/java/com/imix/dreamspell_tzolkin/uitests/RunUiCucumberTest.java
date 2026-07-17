package com.imix.dreamspell_tzolkin.uitests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Appium BDD runner. Run locally with a booted device + Appium server:
 *   ./gradlew :uitests:uiTest
 * Screenshots land in uitests/build/appium-screenshots/.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.imix.dreamspell_tzolkin.uitests",
        plugin = {"pretty", "html:build/cucumber-report.html"}
)
public class RunUiCucumberTest {
}
