package com.imix.dreamspell_tzolkin.bdd

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

/**
 * BDD entry point. NOT part of the default unit run — invoke it explicitly:
 *   ./gradlew :app:testDebugUnitTest --tests "com.imix.dreamspell_tzolkin.bdd.RunCucumberTest"
 */
@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["classpath:features"],
    glue = ["com.imix.dreamspell_tzolkin.bdd"],
    plugin = ["pretty", "summary"],
)
class RunCucumberTest
