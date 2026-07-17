package com.imix.dreamspell_tzolkin.uitests;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KinCombinatorSteps {

    private final World world;

    public KinCombinatorSteps(World world) {
        this.world = world;
    }

    @Given("I open the Kin Combinator")
    public void openKinCombinator() {
        world.tap(world.text("More"));
        world.tap(world.text("Kin Combinator"));
        assertTrue("empty state not shown", world.isPresent(world.textContains("No kins yet")));
    }

    @When("I add {int} kins from the Tzolkin picker")
    public void addFromTzolkin(int count) {
        world.tap(world.text("From Tzolkin"));
        world.tap(world.text("Pick from Tzolkin"));
        world.waitFor(world.text("Tap kins to select"));
        // tap the first N tone cells of the grid (any selectable cells will do)
        List<WebElement> cells = world.driver.findElements(world.id("kcPickerTableHost"));
        // fall back to tapping distinct tone numbers if id lookup returns the host container
        int tapped = 0;
        for (String tone : new String[]{"1", "2", "3", "4", "5"}) {
            if (tapped >= count) break;
            List<WebElement> byTone = world.driver.findElements(world.text(tone));
            if (!byTone.isEmpty()) {
                byTone.get(0).click();
                tapped++;
            }
        }
        assertTrue("selected fewer than requested", tapped >= count);
        // the Add button shows the live count, e.g. "Add (2)"
        world.tap(world.textContains("Add ("));
    }

    @Then("the combined result is shown")
    public void combinedShown() {
        assertTrue("combined caption missing", world.isPresent(world.textContains("Combined")));
        assertTrue("combined kin name missing", world.isPresent(world.textContains("Kin ")));
        world.screenshot("kin_combinator_result");
    }

    @When("I clear all kins")
    public void clearAll() {
        world.tap(world.text("Clear all"));
    }

    @Then("the empty state is shown again")
    public void emptyAgain() {
        assertTrue(world.isPresent(world.textContains("No kins yet")));
        assertFalse("result should be gone", world.isPresent(world.textContains("Combined")));
    }
}
