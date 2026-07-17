Feature: Day navigation (‹ Today › stepper)
  The stepper moves the selected date by one day and offers a jump back to today.
  "Today" is only meaningful (active) when the selected date is not already today.

  Scenario: next then previous returns to the same day
    Given the date 2026-07-10
    When I step to the next day
    And I step to the previous day
    Then the selected day is 2026-07-10

  Scenario: stepping forward one day advances the kin by one
    Given the date 2026-07-10
    When I step to the next day
    Then the kin advanced by one from the previous day

  Scenario: Today is muted when the selection is already today
    Given the date is today
    Then the Today action is muted

  Scenario: Today is active after navigating away
    Given the date is today
    When I step to the next day
    Then the Today action is active
