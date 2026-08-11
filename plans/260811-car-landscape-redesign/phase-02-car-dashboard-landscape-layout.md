# Phase 02: Car Dashboard Landscape Layout Redesign

**Status:** ✅ Completed  
**Dependencies:** Phase 01  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Redesign the main Car mode interface (`layout-land/activity_car.xml` and `CarActivity.kt`) into a driver-optimized widescreen 3-section layout:
1. **Left Driver Rail (Width: ~88dp):** Quick-access buttons placed right next to the driver's hand (Back, Wi-Fi status dialog trigger, Supabase sync status badge, Starred Favorite 1-tap launcher, Settings).
2. **Center Primary Hero Card (Width: ~55% of remaining space):** Synced location display (title, clean address, sync timestamp badge) and a massive 68dp+ **"MỞ BẢN ĐỒ / START NAVIGATION"** button.
3. **Right Quick Favorites / Recents Panel (Width: ~45% of remaining space):** Direct one-tap favorite destinations list (`rvQuickFavorites`) with Star badges and instant navigation.

---

## 📋 Requirements

### Functional Requirements
1. **Left Driver Control Rail (`layout_driver_rail`):**
   - Back button (`btnBack`): 56dp x 56dp icon button.
   - Wi-Fi status indicator/button (`btnWifiSettings`): shows Wi-Fi status and opens Wi-Fi dialog.
   - Cloud Sync status badge (`ivSyncStatus`): green pulsing dot when connected to Supabase, amber when reconnecting.
   - Direct Starred Favorite button (`btnStarredQuick`): single-tap directly opens Starred place on Google Maps.
   - Settings button (`btnSettings`): opens Settings Activity.
2. **Central Synced Location Card (`cardCurrentLocation`):**
   - Status header: "ĐÃ ĐỒNG BỘ TỪ ĐIỆN THOẠI" + timestamp badge (`tvSyncTime`).
   - Location Title (`tvLocationName`): bold 26sp+ high-contrast white text, max 2 lines.
   - Location Subtitle/Address (`tvLocationAddress` / `tvLocation`): clean address text.
   - Giant Navigation Action Button (`btnOpenMap`): min height 68dp, vibrant green background, Google Maps icon, text "MỞ BẢN ĐỒ".
   - Reload / Refresh button (`btnReload`): 56dp quick sync button.
3. **Right Quick Favorites Panel (`cardQuickFavorites`):**
   - Header: "⭐ ĐỊA ĐIỂM ƯA THÍCH" + View All / Double-Tap hint.
   - RecyclerView (`rvQuickFavorites`) displaying top favorite items in high-contrast horizontal/vertical chips.
   - Double-tap gesture support on the panel / favorites button to trigger the full floating modal popup.

### Non-Functional Requirements
- Support both 16:9 (1280x720, 1920x1080) and 21:9 (1920x720) widescreen automotive screens.
- Keep screen awake (`android:keepScreenOn="true"`).

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Create `app/src/main/res/layout-land/activity_car.xml` with the 3-section layout.
2. [x] **Step 2:** Update `app/src/main/res/layout/item_quick_favorite.xml` for horizontal car screen tiles.
3. [x] **Step 3:** Update `CarActivity.kt` to bind the new landscape controls (Quick favorites list, sync status indicator, driver rail handlers).
4. [x] **Step 4:** Ensure single-tap on quick favorite items immediately dispatches navigation intent (`openMap(url)`).
5. [x] **Step 5:** Create file-based unit/layout verification tests: `CarLandscapeLayoutXmlTest.kt` and `CarDashboardComponentTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout-land/activity_car.xml` - [NEW] Dedicated automotive landscape layout
- `app/src/main/res/layout/item_quick_favorite.xml` - [NEW] Quick favorite item layout for in-car dashboard
- `app/src/main/java/com/skul9x/locateshare/CarActivity.kt` - [MODIFY] Support landscape view bindings & quick favorites adapter
- `app/src/main/java/com/skul9x/locateshare/adapter/QuickFavoriteAdapter.kt` - [NEW] Lightweight adapter for in-car quick favorites panel
- `app/src/test/java/com/skul9x/locateshare/layout/CarLandscapeLayoutXmlTest.kt` - [NEW] XML hierarchy and ID verification test
- `app/src/test/java/com/skul9x/locateshare/layout/CarDashboardComponentTest.kt` - [NEW] Component binding & touch target size test

---

## 🧪 Detailed File-Based Test Criteria

### Test 1: `CarLandscapeLayoutXmlTest.kt`
- Parses `layout-land/activity_car.xml` with DOM/XML parser.
- Asserts presence of required IDs: `rootCarLayout`, `btnBack`, `btnWifiSettings`, `btnSettings`, `tvLocationName`, `btnOpenMap`, `btnReload`, `rvQuickFavorites`, `ivSyncStatus`.
- Asserts that `btnOpenMap` has `layout_height >= 60dp` (or dimension reference).

### Test 2: `CarDashboardComponentTest.kt`
- Tests `CarActivity` initialization in landscape orientation via Robolectric / JUnit test.
- Verifies click on `btnOpenMap` invokes map launch intent with correct parsed URL.
- Verifies click on quick favorite item triggers direct map intent.

---

Next Phase: [Phase 03: Favorites Floating Modal Landscape](phase-03-favorites-floating-modal-landscape.md)
