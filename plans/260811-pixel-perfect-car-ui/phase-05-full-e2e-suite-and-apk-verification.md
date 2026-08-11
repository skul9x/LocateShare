# Phase 05: Full E2E Suite & APK Verification

**Status:** ✅ Completed  
**Dependencies:** Phase 01, Phase 02, Phase 03, Phase 04  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Execute the entire test suite and verify end-to-end functionality, build the debug APK, and deploy/test on the connected device via ADB MCP:
1. **Master Test Suite Execution:** Run all existing 140+ unit and layout tests alongside new pixel-perfect test suites (`./gradlew test`).
2. **E2E Layout Integration Test:** Verify all landscape XML files, manifest metadata, and color/dimen tokens.
3. **APK Build & Device Verification:** Assemble debug APK, install via ADB MCP, and verify on-screen UI rendering.

---

## 📋 Requirements

### Functional Requirements
1. 100% passing test rate across all test suites (0 regressions).
2. Successful APK build (`app-debug.apk`).
3. Deploy and verify on connected Android device via ADB MCP.

### Non-Functional Requirements
- APK builds cleanly without warnings or lint errors.
- Smooth 60fps performance on target hardware.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Create `PixelPerfectE2EIntegrationTest.kt` covering all layouts and components.
2. [x] **Step 2:** Run full Gradle test suite (`./gradlew test`).
3. [x] **Step 3:** Assemble debug APK (`./gradlew assembleDebug`).
4. [x] **Step 4:** Install and verify on device via ADB MCP (`adb_install`, `dump_image`).

---

## 📁 Files to Create/Modify
- `app/src/test/java/com/skul9x/locateshare/layout/PixelPerfectE2EIntegrationTest.kt` - [NEW] Master E2E integration test
- `plans/260811-pixel-perfect-car-ui/phase-05-full-e2e-suite-and-apk-verification.md` - [MODIFY] Mark status upon completion

---

## 🧪 Detailed File-Based Test Criteria

### Test: `PixelPerfectE2EIntegrationTest.kt`
- Verifies all landscape XML files exist, parse without XML errors, and include all required view IDs.
- Verifies automotive design tokens, button heights (`72dp`), and driver rail widths (`88dp`).
- Verifies AndroidManifest automotive tags and `distractionOptimized="true"`.
