# Phase 02: Driver Rail & Hero Card Redesign

**Status:** ✅ Completed  
**Dependencies:** Phase 01  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Redesign the Left Driver Rail and Center Primary Hero Card in `layout-land/activity_car.xml` to match the exact visual layout of the design mockup:
1. **Left Driver Rail:**
   - Obsidian column (`#101217`) with uniform padding and 12dp icon spacing.
   - Rounded square icon tiles (`56dp x 56dp`) with subtle stroke for Back, Wi-Fi, Cloud Sync indicator, Starred Fast Launcher, and Settings.
   - Dedicated sync status container with centered indicator dot and subtle glow.
2. **Center Primary Hero Card:**
   - Dark elevated surface (`#161922`), corner radius `20dp`, and subtle border stroke (`#252938`).
   - Top sync pill badge: `● ĐÃ ĐỒNG BỘ TỪ ĐIỆN THOẠI  •  14:49:06`.
   - Large bold location header (`24sp`, `#FFFFFF`), followed by address subtitle (`14sp`, `#8E95A5`).
   - Bottom action container with oversized `72dp` green `"MỞ BẢN ĐỒ"` button and adjacent `72dp` square reload button.

---

## 📋 Requirements

### Functional Requirements
1. Maintain all existing view IDs (`rootCarLayout`, `layout_driver_rail`, `btnBack`, `btnWifiSettings`, `ivSyncStatus`, `btnStarredQuick`, `btnSettings`, `cardCurrentLocation`, `tvSyncHeader`, `tvSyncTime`, `tvLocationName`, `tvLocation`, `btnOpenMap`, `btnReload`).
2. Implement exact visual hierarchy and padding matching the mockup.
3. Keep edge-to-edge `WindowInsetsCompat` support in `CarActivity.kt`.

### Non-Functional Requirements
- Touch targets >= 56dp for rail icons and 72dp for main action button.
- Smooth visual scaling across 1280x720, 1920x1080, and 1920x720 widescreen head units.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Update `app/src/main/res/layout-land/activity_car.xml` with the new Driver Rail and Hero Card layout hierarchy.
2. [x] **Step 2:** Ensure `CarActivity.kt` updates the pill sync badge and button states correctly.
3. [x] **Step 3:** Create and execute file-based verification test `PixelPerfectCarDashboardLayoutTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout-land/activity_car.xml` - [MODIFY] Redesign Driver Rail & Center Card
- `app/src/main/java/com/skul9x/locateshare/CarActivity.kt` - [MODIFY] Sync badge and UI formatting updates
- `app/src/test/java/com/skul9x/locateshare/layout/PixelPerfectCarDashboardLayoutTest.kt` - [NEW] File-based verification test

---

## 🧪 Detailed File-Based Test Criteria

### Test: `PixelPerfectCarDashboardLayoutTest.kt`
- Verifies that `layout-land/activity_car.xml` contains all required view IDs.
- Verifies that the Hero action button (`btnOpenMap`) has height token `72dp` or `@dimen/car_hero_button_height_large`.
- Verifies that Driver Rail buttons have minimum dimension `56dp`.
- Verifies that `cardCurrentLocation` specifies corner radius `20dp` and dark card surface color.
