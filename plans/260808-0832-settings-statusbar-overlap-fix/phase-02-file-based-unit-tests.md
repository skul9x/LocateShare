# Phase 02: File-Based XML & Insets Unit Tests
Status: ✅ Complete
Dependencies: [Phase 01](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0832-settings-statusbar-overlap-fix/phase-01-statusbar-insets-fix.md)

## Objective
Create detailed automated file-based JUnit test coverage (`SettingsStatusbarInsetsTest.kt`) to verify that both portrait and landscape `activity_settings.xml` files enforce `android:fitsSystemWindows="true"` and preserve all required views and attributes.

## Requirements
### Functional
- [x] Test portrait `activity_settings.xml` root node has attribute `android:fitsSystemWindows="true"`.
- [x] Test landscape `activity_settings.xml` root node has attribute `android:fitsSystemWindows="true"`.
- [x] Verify both layouts retain all critical functional view IDs:
  - `btnBackSettings`
  - `etFavName`
  - `etFavUrl`
  - `btnAddFavorite`
  - `rvFavorites`
- [x] Verify Gradle test suite execution passes completely.

### Non-Functional
- [x] Tests must execute fast as JVM unit tests without requiring Android device / emulator runtime.
- [x] Accurate failure assertions with clear diagnostic messages.

## Implementation Steps
1. [x] Create test file `app/src/test/java/com/skul9x/locateshare/layout/SettingsStatusbarInsetsTest.kt`.
2. [x] Implement DOM-based XML parsing for both `app/src/main/res/layout/activity_settings.xml` and `app/src/main/res/layout-land/activity_settings.xml`.
3. [x] Run `./gradlew testDebugUnitTest --tests "com.skul9x.locateshare.layout.SettingsStatusbarInsetsTest"`.
4. [x] Run full project test suite `./gradlew test` to ensure zero regressions.

## Files to Create/Modify
- `app/src/test/java/com/skul9x/locateshare/layout/SettingsStatusbarInsetsTest.kt` - New JUnit test class for XML insets verification.

## Test Criteria
- [x] `SettingsStatusbarInsetsTest.testPortraitFitsSystemWindows()` passes.
- [x] `SettingsStatusbarInsetsTest.testLandscapeFitsSystemWindows()` passes.
- [x] `SettingsStatusbarInsetsTest.testRequiredViewsPreserved()` passes.
- [x] Full test execution (`./gradlew test`) succeeds with 0 failures.

## Notes
- Works seamlessly in CI/CD environments as standard JUnit XML validation tests.
