Feature: Destiny Oracle
  The Oracle screen shows five related kin around the day's destiny kin:
  guide, analog, antipode and occult. They must stay internally consistent.

  Scenario Outline: the oracle kin are valid and share the destiny tone
    Given the destiny kin <kin>
    Then every oracle kin is between 1 and 260
    And the guide, analog and antipode kin share the destiny tone

    Examples:
      | kin |
      | 1   |
      | 60  |
      | 130 |
      | 199 |
      | 260 |

  Scenario: occult is the mirror across 261
    Given the destiny kin 200
    Then the occult kin is 61

  Scenario: antipode is 130 kin away
    Given the destiny kin 1
    Then the antipode kin is 131
