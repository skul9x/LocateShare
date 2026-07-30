# Phase 03: Unit & Integration Verification [COMPLETED]

## Objective
Add a comprehensive suite of unit tests in `app/src/test/java/com/skul9x/locateshare/util/WifiSettingsIntentTest.kt` and `CarLayoutXmlTest.kt` to ensure complete code quality, correct intent creation, WindowInsets configuration, and XML UI file structure integrity.

## Requirements
- Verify that `Settings.ACTION_WIFI_SETTINGS` action string equals `"android.settings.WIFI_SETTINGS"`.
- Verify helper intent builder creates valid Intent instances targeting system Wi-Fi settings.
- Verify XML layout file contains all required view IDs (`btnWifiSettings`, `rootCarLayout`), `fitsSystemWindows="true"`, and drawable bindings.
- Ensure `./gradlew test` passes cleanly with 0 test failures.

## Implementation Steps
1. **Create Intent Unit Test**: Implement `WifiSettingsIntentTest.kt` under `app/src/test/java/com/skul9x/locateshare/util/`.
2. **Create Layout XML Structural Test**: Implement `CarLayoutXmlTest.kt` under `app/src/test/java/com/skul9x/locateshare/util/`.
3. **Execute Gradle Tests**: Run `./gradlew test` to execute all unit tests in the project.

## Files to Create/Modify
- `[NEW]` [WifiSettingsIntentTest.kt](file:///home/skul9x/Desktop/Test_code/LocateShare-main/app/src/test/java/com/skul9x/locateshare/util/WifiSettingsIntentTest.kt)
- `[NEW]` [CarLayoutXmlTest.kt](file:///home/skul9x/Desktop/Test_code/LocateShare-main/app/src/test/java/com/skul9x/locateshare/util/CarLayoutXmlTest.kt)

## File-Based Phase Verification Test
Run automated build and unit test task:
```bash
./gradlew test
```
Confirm build output succeeds and test suite executes `testWifiIntentAction()` and `testCarLayoutXmlContainsWifiButtonAndFitsSystemWindows()`.
