package com.imix.dreamspell_tzolkin.uitests;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertTrue;

public class NavigationSteps {

    private final World world;

    public NavigationSteps(World world) {
        this.world = world;
    }

    @Given("the app is on the Galactic Signature screen")
    public void onSignature() {
        world.tap(world.text("Signature"));
        assertTrue("kin caption not shown", world.isPresent(world.textContains("Kin ")));
    }

    @When("I tap the {string} tab")
    public void tapTab(String label) {
        world.tap(world.text(label));
    }

    @When("I swipe left")
    public void swipeLeft() {
        world.swipeLeft();
    }

    @When("I swipe right")
    public void swipeRight() {
        world.swipeRight();
    }

    @When("I open the More menu")
    public void openMore() {
        world.tap(world.text("More"));
    }

    @Then("the {string} screen is shown")
    public void screenShown(String label) {
        // each primary shows its selected nav label; secondary screens show their own title text
        assertTrue("expected '" + label + "' to be visible", world.isPresent(world.text(label)));
        world.screenshot("screen_" + label);
    }

    @Then("the More menu lists {string}")
    public void moreLists(String label) {
        assertTrue("'" + label + "' missing from More", world.isPresent(world.text(label)));
    }

    @Then("today's kin is shown in the caption")
    public void todaysKin() {
        assertTrue(world.isPresent(world.textContains("Kin ")));
    }
}
