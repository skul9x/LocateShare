# Phase 03: Favorites Floating Modal Landscape Redesign

**Status:** ✅ Completed  
**Dependencies:** Phase 01, Phase 02  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Redesign the Double-Tap Floating Favorites Modal (`dialog_favorites_card_popup.xml` and `item_favorite_card.xml`) for landscape in-car screens, transforming it from a single narrow column into an expansive, driver-friendly 2-column or 3-column card grid with 85% Frosted Background Blur (`FLAG_BLUR_BEHIND`, `dimAmount = 0.85f`) and oversized touch targets (>=60dp).

---

## 📋 Requirements

### Functional Requirements
1. **Widescreen Modal Container (`dialog_favorites_card_popup.xml` in `layout-land`):**
   - Modal width: 85% of screen width, max width 1100dp.
   - Background: Frosted dark surface `#1E1E1E` with subtle border stroke `#2E3240` and 16dp rounded corners.
   - Header: "⭐ ĐỊA ĐIỂM ƯA THÍCH" + Close Button (56dp target).
   - Starred default badge indicator on top of modal.
2. **2-Column Grid Layout for Favorites (`item_favorite_card.xml`):**
   - RecyclerView configured with `GridLayoutManager(context, 2)` when in landscape.
   - Card content: Star icon (⭐) for default favorite, Destination Title (20sp bold white), snippet of address (14sp gray).
   - Action: Massive green **"CHỈ ĐƯỜNG / NAVIGATE"** button (60dp height) or entire card tap trigger.
3. **85% Frosted Glass & Dimming Blur:**
   - Maintain `dimAmount = 0.85f`.
   - Apply `FLAG_BLUR_BEHIND` and `blurBehindRadius = 60` for API 31+ (Android 12 & Android 13+).

### Non-Functional Requirements
- Scroll performance: 60fps smooth scrolling with large scrollbar thumb for easy tactile visual feedback.
- Accessible touch targets for moving vehicle interaction (parked or driving).

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Create `app/src/main/res/layout-land/dialog_favorites_card_popup.xml` with widescreen dialog container.
2. [x] **Step 2:** Update `item_favorite_card.xml` with card width/height constraints suited for a 2-column grid.
3. [x] **Step 3:** Update `FavoriteCardAdapter.kt` and `CarActivity.showFavoritesPopup()` to initialize `GridLayoutManager(this, 2)` in landscape orientation.
4. [x] **Step 4:** Ensure single tap on any item dismisses the dialog and launches navigation without lag.
5. [x] **Step 5:** Create file-based unit/layout verification tests: `FavoritesFloatingModalLandscapeTest.kt` and `FavoriteCardAdapterLandscapeTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout-land/dialog_favorites_card_popup.xml` - [NEW] Widescreen modal popup layout
- `app/src/main/res/layout/item_favorite_card.xml` - [MODIFY] Touch-optimized favorite card for 2-column grid
- `app/src/main/java/com/skul9x/locateshare/CarActivity.kt` - [MODIFY] Adaptive GridLayoutManager configuration
- `app/src/main/java/com/skul9x/locateshare/adapter/FavoriteCardAdapter.kt` - [MODIFY] Support grid item styling and starred badges
- `app/src/test/java/com/skul9x/locateshare/layout/FavoritesFloatingModalLandscapeTest.kt` - [NEW] Modal XML layout verification test
- `app/src/test/java/com/skul9x/locateshare/adapter/FavoriteCardAdapterLandscapeTest.kt` - [NEW] Adapter binding and grid span test

---

## 🧪 Detailed File-Based Test Criteria

### Test 1: `FavoritesFloatingModalLandscapeTest.kt`
- Parses `layout-land/dialog_favorites_card_popup.xml`.
- Asserts presence of `rvFavoriteCards`, `btnClose`, `tvDialogTitle`, `tvEmptyFavorites`.
- Checks that dialog container specifies appropriate landscape margins and padding.

### Test 2: `FavoriteCardAdapterLandscapeTest.kt`
- Tests `FavoriteCardAdapter` with a mock dataset containing 4 favorites (1 starred).
- Asserts that item 0 has `is_starred = true` and shows the ⭐ badge.
- Asserts that clicking the action button dispatches the correct item URL to the listener.

---

Next Phase: [Phase 04: Settings Split-Screen Landscape](phase-04-settings-split-screen-landscape.md)
