Feature: Kin Combinator
  Combine several kins into one. Kins can be added from the Tzolkin picker;
  the combined result appears below the list and clearing returns to the empty state.

  Background:
    Given the app is on the Galactic Signature screen

  Scenario: Combine two kins from the Tzolkin picker
    Given I open the Kin Combinator
    When I add 2 kins from the Tzolkin picker
    Then the combined result is shown

  Scenario: Clearing all kins returns to the empty state
    Given I open the Kin Combinator
    When I add 3 kins from the Tzolkin picker
    Then the combined result is shown
    When I clear all kins
    Then the empty state is shown again
