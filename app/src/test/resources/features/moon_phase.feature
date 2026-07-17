Feature: Moon phase naming
  A lunar elongation angle (0 = new, 180 = full) maps to one of eight named phases.

  Scenario Outline: angle maps to the correct phase
    Given a moon elongation of <angle> degrees
    Then the moon phase is "<phase>"

    Examples:
      | angle | phase           |
      | 0     | NEW             |
      | 45    | WAXING_CRESCENT |
      | 90    | FIRST_QUARTER   |
      | 135   | WAXING_GIBBOUS  |
      | 180   | FULL            |
      | 225   | WANING_GIBBOUS  |
      | 270   | LAST_QUARTER    |
      | 320   | WANING_CRESCENT |
