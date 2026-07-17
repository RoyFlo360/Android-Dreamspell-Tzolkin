# uitests — Appium BDD UI tests

End-to-end Cucumber/Gherkin scenarios driven through Appium against a real
device/emulator. Isolated from `:app` so `:app:testDebugUnitTest` never loads
Appium or touches a device.

## What's here

- `features/*.feature` — Gherkin scenarios (navigation, Kin Combinator).
- `*Steps.java` — step definitions using the `World` element helpers.
- `World.java` — one `AndroidDriver` session per scenario, plus `id()`/`text()`/
  `textContains()`/`tap()`/`swipe*()`/`screenshot()`. Screenshots land in
  `uitests/build/appium-screenshots/`; one is taken at the end of every scenario.
- `RunUiCucumberTest.java` — the Cucumber runner (`:uitests:uiTest`).

## Prerequisites (one-time)

```bash
npm install -g appium
appium driver install uiautomator2
```
Node 18+ and a JDK 17 are required.

## Run

1. Boot a device/emulator and confirm `adb devices` lists it.
2. Install the app under test:
   ```bash
   ./gradlew :app:assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Start the Appium server (separate terminal):
   ```bash
   appium
   ```
4. Run the suite:
   ```bash
   ./gradlew :uitests:uiTest
   ```

Report: `uitests/build/cucumber-report.html`. Screenshots:
`uitests/build/appium-screenshots/`.

## Overrides

| Property         | Default                   | Purpose                     |
|------------------|---------------------------|-----------------------------|
| `appium.server`  | `http://127.0.0.1:4723/`  | Appium server URL           |
| `appium.device`  | `Android`                 | UiAutomator2 device name    |

```bash
./gradlew :uitests:uiTest -Dappium.device="Pixel_7_API_34"
```

The app is driven with `noReset=true` — it uses the already-installed build and
keeps its language/prefs. These tests need a device + Appium server, so they run
on demand only, never as part of `build`/`check`.
