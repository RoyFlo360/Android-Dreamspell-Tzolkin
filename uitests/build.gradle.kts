// Standalone JVM module for Appium BDD UI tests. Fully isolated from :app so the fast JVM
// unit tests (:app:testDebugUnitTest) never try to load Appium or reach a device.
// Run locally against a booted device + a running Appium server: ./gradlew :uitests:uiTest
plugins {
    java
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit)
    testImplementation(libs.appium.java.client)
}

// Not part of `check`/`build` — these need a device + Appium server, so they run only on demand.
tasks.named<Test>("test") { enabled = false }

tasks.register<Test>("uiTest") {
    group = "verification"
    description = "Appium BDD UI tests. Requires a booted device and an Appium server on :4723."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnit()
    outputs.upToDateWhen { false }
    testLogging { events("passed", "failed", "skipped") }
}
