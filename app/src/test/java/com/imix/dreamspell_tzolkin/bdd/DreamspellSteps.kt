package com.imix.dreamspell_tzolkin.bdd

import com.imix.dreamspell_tzolkin.Dreamspell
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Step definitions for all .feature files. They exercise the pure [Dreamspell] domain (JVM only,
 * no Android), so BDD runs as plain unit tests. Run locally with:
 *   ./gradlew :app:testDebugUnitTest --tests "com.imix.dreamspell_tzolkin.bdd.RunCucumberTest"
 */
class DreamspellSteps {

    private var destinyKin = 0
    private var date: Calendar = Calendar.getInstance()
    private val now: Calendar = Calendar.getInstance()
    private var previousKin = 0
    private var angle = 0.0

    private fun parseDate(iso: String): Calendar {
        val (y, m, d) = iso.split("-").map { it.toInt() }
        return GregorianCalendar(y, m - 1, d, 12, 0, 0)
    }

    // ---- Destiny Oracle ----

    @Given("the destiny kin {int}")
    fun theDestinyKin(kin: Int) { destinyKin = kin }

    @Then("every oracle kin is between 1 and 260")
    fun everyOracleKinInRange() {
        listOf(
            destinyKin,
            Dreamspell.guideKin(destinyKin),
            Dreamspell.analogKin(destinyKin),
            Dreamspell.antipodeKin(destinyKin),
            Dreamspell.occultKin(destinyKin),
        ).forEach { assertTrue("kin $it out of range", it in 1..260) }
    }

    @Then("the guide, analog and antipode kin share the destiny tone")
    fun oracleKinShareTone() {
        val t = Dreamspell.tone(destinyKin)
        assertEquals(t, Dreamspell.tone(Dreamspell.guideKin(destinyKin)))
        assertEquals(t, Dreamspell.tone(Dreamspell.analogKin(destinyKin)))
        assertEquals(t, Dreamspell.tone(Dreamspell.antipodeKin(destinyKin)))
    }

    @Then("the occult kin is {int}")
    fun theOccultKinIs(expected: Int) = assertEquals(expected, Dreamspell.occultKin(destinyKin))

    @Then("the antipode kin is {int}")
    fun theAntipodeKinIs(expected: Int) = assertEquals(expected, Dreamspell.antipodeKin(destinyKin))

    // ---- Dates / PSI / navigation ----

    @Given("the date {word}")
    fun theDate(iso: String) { date = parseDate(iso) }

    @Given("the date is today")
    fun theDateIsToday() { date = now.clone() as Calendar }

    @When("I step to the next day")
    fun stepNext() { previousKin = Dreamspell.kinFor(date); date.add(Calendar.DAY_OF_YEAR, 1) }

    @When("I step to the previous day")
    fun stepPrev() { date.add(Calendar.DAY_OF_YEAR, -1) }

    @Then("the selected day is {word}")
    fun theSelectedDayIs(iso: String) = assertTrue(Dreamspell.isSameDay(date, parseDate(iso)))

    @Then("the kin advanced by one from the previous day")
    fun kinAdvancedByOne() = assertEquals(previousKin % 260 + 1, Dreamspell.kinFor(date))

    @Then("the Today action is muted")
    fun todayMuted() = assertTrue(Dreamspell.isSameDay(date, now))

    @Then("the Today action is active")
    fun todayActive() = assertFalse(Dreamspell.isSameDay(date, now))

    @Then("the PSI kin is {int}")
    fun thePsiKinIs(expected: Int) = assertEquals(expected, Dreamspell.psiKinFor(date))

    @Then("there is no PSI kin")
    fun noPsiKin() = assertNull(Dreamspell.psiKinFor(date))

    @Then("the PSI kin has a valid seal and tone")
    fun psiSealAndTone() {
        val psi = Dreamspell.psiKinFor(date)!!
        assertTrue(Dreamspell.seal(psi) in 1..20)
        assertTrue(Dreamspell.tone(psi) in 1..13)
    }

    // ---- Moon phase ----

    @Given("a moon elongation of {int} degrees")
    fun aMoonElongation(deg: Int) { angle = deg.toDouble() }

    @Then("the moon phase is {string}")
    fun theMoonPhaseIs(name: String) =
        assertEquals(Dreamspell.MoonPhase.valueOf(name), Dreamspell.moonPhaseName(angle))
}
