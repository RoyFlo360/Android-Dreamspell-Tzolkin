# Testing

Quality + maintainability are first-class (ISO/IEC 25010). Tests split into two
suites: **unit** (plain JVM, run in CI/locally, fast) and **BDD** (Cucumber/
Gherkin, run locally on demand — not in CI). No Robolectric/Espresso/MockK:
all logic under test is pure (`Dreamspell.kt`) or a pure parser.

## Run

```bash
# Unit tests (48, ~7s, offline)
./gradlew :app:testDebugUnitTest \
  --tests "com.imix.dreamspell_tzolkin.DreamspellMathTest" \
  --tests "com.imix.dreamspell_tzolkin.MoonPhaseNameTest" \
  --tests "com.imix.dreamspell_tzolkin.ParseRecordsTest" \
  --tests "com.imix.dreamspell_tzolkin.DayNavigationStateTest"

# BDD / Gherkin (run locally — needs network on first run for Cucumber deps)
./gradlew :app:testDebugUnitTest --tests "com.imix.dreamspell_tzolkin.bdd.RunCucumberTest"
```

## Unit suites (48 tests)

| Class | Count | Covers |
|-------|-------|--------|
| `DreamspellMathTest` | 21 | kin/tone/seal/wavespell/occult/antipode/guide/analog; BVA (kin 1/2/130/131/260/261-wrap); involution (occult∘occult, antipode∘antipode); every (seal,tone) unique; leap-day noon repeat |
| `MoonPhaseNameTest` | 10 | `moonPhaseName` decision table — 8 phases + boundary angles (7/83/97/173/187/263/277/353) + normalization of negative/>360 |
| `ParseRecordsTest` | 10 | pure StAX parser: empty file, missing field→"", weird chars (accented/CJK/Cyrillic/emoji), XML entities, multiline text, malformed |
| `DayNavigationStateTest` | 7 | `isSameDay` + stepper state transitions (kin wrap 260→1, year Dec31→Jan1, into/out of Day-Out-of-Time) |

## Applicable test types → where

Unit/functional, BVA, decision tables, state transitions, property/involution,
gray-box integration, empty/null, weird-character — all in the four classes
above and the `.feature` files.

## N/A ledger (documented, not built — with the *why*)

- **Security / auth / injection** — no network, no auth, no user-supplied
  strings, no IPC beyond the OS date picker (bounded ints). Only external input
  is app-owned `res/raw`. Parser hardened anyway: StAX with `SUPPORT_DTD=false`
  and `IS_SUPPORTING_EXTERNAL_ENTITIES=false` (no XXE).
- **API / integration (network)** — no APIs exist.
- **JSON / dict / SQL empties** — no JSON, no DB, no maps as inputs.
- **Performance** — all math is O(1) or O(260) over fixed tables; no hot path.

## ISO/IEC 25010 mapping

- **Functional Suitability** → correctness suite (`DreamspellMathTest`).
- **Reliability** → fault/edge tests (kin wrap, null PSI on Day-Out-of-Time,
  malformed XML).
- **Maintainability** → pure-logic isolation, `moonPhaseName`/`isSameDay`/
  parser extractions, tests-as-docs.
- **Portability** → minSdk 26 + locale-variant parser tests.
- **Compatibility** → `DreamspellData` locale-cache invalidation.
- **Usability** → the nav/UX revamp (verified manually).
- **Performance Efficiency / Security** → N/A ledger above.
