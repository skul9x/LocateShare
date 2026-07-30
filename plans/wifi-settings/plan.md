# Feature Plan: Wi-Fi Settings Access & Status Bar Overlap Fix in Car Mode

## Overview
This plan adds a dedicated Wi-Fi icon button to the landscape Car Mode screen (`CarActivity`) of LocateShare and fixes potential **Status Bar Overlap** issues (where Android top system status bar/cutouts overlap UI elements like `btnBack`, `tvTitle`, `btnWifiSettings`, and `btnSettings`).

## Tech Stack & APIs
- **Platform**: Android (Kotlin, SDK 24+)
- **Layout**: ConstraintLayout (`activity_car.xml`)
- **System Insets Handling**: `androidx.core.view.ViewCompat` & `WindowInsetsCompat`
- **Icon**: Vector Drawable (`ic_wifi.xml`)
- **Android System API**: `android.provider.Settings.ACTION_WIFI_SETTINGS`
- **Testing**: JUnit 4 unit test & XML layout parser verification

## Phase Breakdown

| Phase | Name | Description | Status |
|---|---|---|---|
| 01 | UI Vector Icon, Layout & Insets Config | Create vector drawable icon `ic_wifi.xml`, add `btnWifiSettings`, and set `android:fitsSystemWindows="true"` + `id="@+id/rootCarLayout"` in `activity_car.xml`. | ✅ Completed |
| 02 | Intent Logic & WindowInsets Handler | Implement `openWifiSettings()` intent and attach `ViewCompat.setOnApplyWindowInsetsListener` in `CarActivity.kt` to dynamically adjust top padding against system status bar overlap. | ✅ Completed |
| 03 | Unit & Integration Verification | Implement file-based tests `WifiSettingsIntentTest.kt` and `CarLayoutXmlTest.kt` to verify WindowInsets config, Intent structure, and run `./gradlew test`. | ✅ Completed |

## Execution Workflow
1. Complete Phase 01 & verify layout XML constraints and root inset flags.
2. Complete Phase 02 & bind click listener and WindowInsets padding logic.
3. Complete Phase 03 & run file-based tests via `./gradlew test`.
