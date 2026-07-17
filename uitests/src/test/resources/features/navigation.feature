Feature: Bottom-nav and swipe navigation
  The four primary screens (Signature, Oracle, Wavespell, Tzolkin) are both
  swipeable and tap-selectable; More is tap-only and lists the secondary screens.

  Background:
    Given the app is on the Galactic Signature screen

  Scenario: Today's kin is shown on launch
    Then today's kin is shown in the caption

  Scenario Outline: Tap each primary tab
    When I tap the "<tab>" tab
    Then the "<tab>" screen is shown

    Examples:
      | tab       |
      | Oracle    |
      | Wavespell |
      | Tzolkin   |
      | Signature |

  Scenario: Swipe across the primaries and back
    When I swipe left
    Then the "Oracle" screen is shown
    When I swipe left
    Then the "Wavespell" screen is shown
    When I swipe right
    Then the "Oracle" screen is shown

  Scenario: More menu lists the secondary screens
    When I open the More menu
    Then the More menu lists "Help"
    And the More menu lists "Kin Combinator"
    And the More menu lists "Moonphase"
    And the More menu lists "13 Moon Calendar"
    And the More menu lists "Holon"
