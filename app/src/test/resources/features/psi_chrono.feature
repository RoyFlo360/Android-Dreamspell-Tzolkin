Feature: PSI Chrono (Akashic hidden seal)
  The Oracle screen also shows the PSI Chrono kin — the hidden "Akashic" seal —
  except on the Day Out of Time, when there is none.

  Scenario: a normal 13-Moon day has a PSI kin
    Given the date 2025-07-26
    Then the PSI kin is 1

  Scenario: the Day Out of Time has no PSI kin
    Given the date 2025-07-25
    Then there is no PSI kin

  Scenario: the PSI seal and tone come from the PSI kin
    Given the date 2025-07-26
    Then the PSI kin has a valid seal and tone
