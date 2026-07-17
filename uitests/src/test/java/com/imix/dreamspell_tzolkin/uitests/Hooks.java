package com.imix.dreamspell_tzolkin.uitests;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** One Appium session per scenario; always screenshot the final state (pass or fail). */
public class Hooks {

    // shared with the step classes via constructor injection (PicoContainer, Cucumber's default)
    private final World world;

    public Hooks(World world) {
        this.world = world;
    }

    @Before
    public void startSession() throws Exception {
        world.start();
    }

    @After
    public void endSession(Scenario scenario) {
        String name = (scenario.getStatus() + "_" + scenario.getName());
        world.screenshot(name);
        world.stop();
    }
}
