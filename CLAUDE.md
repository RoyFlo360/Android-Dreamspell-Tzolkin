# CLAUDE.md

Guidance for Claude Code (and other agents) working in this repo.

## What this is

**Dreamspell Calendar** — a Kotlin Android app for the Dreamspell / Tzolkin
calendar. It shows the galactic signature (tone + seal) for any date, the
Destiny Oracle "flower" with the PSI Chrono seal, the full 260-kin Tzolkin
grid, the wavespell, the 13-Moon calendar, the moon phase, and a Kin
Combinator. Content ships in 9 languages.

- Package `com.imix.dreamspell_tzolkin`.
- `minSdk 26`, `target`/`compile 34`.
- Modules: `:app` (the app) and `:uitests` (on-demand Appium UI tests).

## Build / install / run

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.imix.dreamspell_tzolkin/.OracleActivity
```

- `--no-configuration-cache` has been used during dev to avoid stale-config issues.
- After a language/manifest change, do a clean reinstall
  (`adb uninstall com.imix.dreamspell_tzolkin` first) if behavior looks stale.

### Release / Play Store

```bash
./gradlew :app:bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab
```

- `bundle { language { enableSplit = false } }` is required: Play otherwise installs only the
  device-language split and the in-app language picker silently falls back to English.
- Signing reads `keystore.properties` at the repo root — **gitignored, never commit it**.
  It points at the upload keystore, which lives outside the repo. Without that file the
  release variant simply builds unsigned rather than failing, so fresh clones and CI work.
- Losing the keystore *and* `keystore.properties` means requesting an upload-key reset
  from Google. Back both up.
- Debug and release are signed with different keys, so `adb install` of one over the other
  fails — `adb uninstall` first.

## Code layout

- **`Dreamspell.kt`** — pure, testable calendar math (kin/tone/seal, Galactic
  Activation Portals, oracle "Fifth Force" partners, PSI Chrono, 13-Moon,
  moon phase). No Android dependencies; unit-tested.
- **`DreamspellData.kt`** — loads localized content (names, descriptions, the
  per-kin oracle text) from `res/raw*/…` XML via SAX, locale-aware and cached.
- **`TabFragments.kt`** — the screen fragments; they wire calendar math + data
  into the layouts in `res/layout/*.xml`.
- **`OracleActivity.kt`** — host activity: a bottom nav over a 4-page swipeable
  `ViewPager2` (Home / Oracle / Wavespell / Tzolkin), a click-only "More" bottom
  sheet (13-Moon, Moon Phase, Help, Kin Combinator, Codex dialogs), the
  ‹ Today › date stepper, and the gear menu (date picker, Language, What's New).
- **`controller/views/`** — custom views: `MoonPhaseView` (lunar disc art with the
  unlit part redrawn darkened through a clip),
  `ZoomStackView` (pinch-zoom Codex images).

## Content & localization

- Text/content lives in `res/raw*/{glyphs,tones,dreamspell,wavespell,thirteenmoon}.xml`,
  one set per locale (`raw/`, `raw-es/`, `raw-fr/`, …). `DreamspellData` parses
  these `<record><field>…</field></record>` files; Android picks the locale
  variant automatically.
- Language switching uses `AppCompatDelegate.setApplicationLocales` + the
  `AppLocalesMetadataHolderService` in the manifest. **The launcher activity must
  NOT declare `configChanges="locale"`**, or in-app locale switching breaks.
- Nahuatl (`nah`) and Yucatec Maya (`yua`) ship via `b+nah` / `b+yua` qualifiers;
  they have no ICU date data, so the action-bar date falls back to Spanish month
  names (see `OracleActivity.updateTitle`).

## Tests

- `./gradlew :app:testDebugUnitTest` — fast JVM unit tests (calendar math, XML parsing).
- `./gradlew :uitests:uiTest` — Appium BDD UI tests; on-demand only (need a booted
  device + a running Appium server). See `uitests/README.md`.
- Test strategy / coverage rationale: `docs/TESTING.md`.

## Docs

Design notes live in `docs/` (`oracle_psi_design.md`, `kin_combinator_design.md`,
`mayan_magic_wisdom_palette.md`, `TESTING.md`).

## Gotchas

- `DreamspellData` caches parsed XML and invalidates on locale change — keep that
  intact or translations go stale.
- `res/` carries redundant AppCompat/Material resources (`abc_*`/`mtrl_*`/`m3_*`).
  Harmless but present; don't be surprised by them.
- The palette in `res/values/palette.xml` is chrome only (backgrounds, bars, nav,
  cards, text) — never tint the `glyphN`/`toneN` seal & tone artwork with it.
- `assets/dexopt/baseline.prof` was decompiled out of the original APK and used to live in
  `src/main/assets/`. AGP generates that exact path for release builds, so it collided and
  broke `packageRelease` (and silently shipped a stale ART profile in the bundle). It is
  deleted — never re-add build outputs under `src/main/assets/`.
