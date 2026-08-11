# Phase 04: Settings Split-Screen Landscape Redesign

**Status:** ✅ Completed  
**Dependencies:** Phase 01  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Overhaul the Settings and Place Management screen (`layout-land/activity_settings.xml` and `SettingsActivity.kt`) into a master-detail split layout:
1. **Left Master Pane (35% width):** App Preferences, Default Starred Destination selector, Supabase Cloud Connection Status badge, and App Version Info.
2. **Right Detail Pane (65% width):** Favorites CRUD Management list with large touch targets, Star toggle (⭐), Edit (✏️), Delete (🗑️), and an oversized "+ THÊM ĐỊA ĐIỂM MỚI" button.

---

## 📋 Requirements

### Functional Requirements
1. **Left Configuration Pane (`layout_settings_left`):**
   - Header: Back to Car button (`btnBack`) (56dp target) + Title "CÀI ĐẶT".
   - Status Card: Realtime Supabase Connection status (Online / Offline indicator + URL).
   - Starred Destination Summary: Quick view of current 1-tap default location.
   - App Version & Information card.
2. **Right Favorites Management Pane (`layout_settings_right`):**
   - Header: "QUẢN LÝ ĐỊA ĐIỂM YÊU THÍCH" + Item Count.
   - RecyclerView (`rvFavorites`): List items (`item_favorite.xml`) with large 56dp action icons (Star, Edit, Delete).
   - Bottom Action Bar: Prominent 60dp "+ THÊM ĐỊA ĐIỂM" primary button (`btnAddFavorite`).
3. **Landscape Add/Edit Dialog (`dialog_edit_favorite.xml`):**
   - Two-column or wide dialog input layout with large text fields for "Tên địa điểm" and "Link Google Maps / Tọa độ".
   - Save / Cancel buttons with 56dp height.

### Non-Functional Requirements
- Automatic status bar and navigation bar insets compensation (`WindowInsetsCompat`).
- Keyboard handling: Ensure input fields do not obscure save buttons when on-screen keyboard appears on car head unit.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Update `app/src/main/res/layout-land/activity_settings.xml` with two-pane split layout.
2. [x] **Step 2:** Update `app/src/main/res/layout/item_favorite.xml` to enhance touch target padding and contrast for in-car editing.
3. [x] **Step 3:** Update `SettingsActivity.kt` to bind the new split-screen elements and status indicators.
4. [x] **Step 4:** Refine `dialog_edit_favorite.xml` for landscape typing and button placement.
5. [x] **Step 5:** Create file-based unit/layout verification tests: `SettingsLandscapeLayoutXmlTest.kt` and `SettingsLandscapeInteractionTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout-land/activity_settings.xml` - [MODIFY] Master-detail split layout for automotive landscape
- `app/src/main/res/layout/item_favorite.xml` - [MODIFY] Touch-safe favorite management item
- `app/src/main/res/layout/dialog_edit_favorite.xml` - [MODIFY] Wide dialog layout
- `app/src/main/java/com/skul9x/locateshare/SettingsActivity.kt` - [MODIFY] View bindings & split-screen handling
- `app/src/test/java/com/skul9x/locateshare/layout/SettingsLandscapeLayoutXmlTest.kt` - [NEW] XML hierarchy test
- `app/src/test/java/com/skul9x/locateshare/layout/SettingsLandscapeInteractionTest.kt` - [NEW] CRUD interaction test

---

## 🧪 Detailed File-Based Test Criteria

### Test 1: `SettingsLandscapeLayoutXmlTest.kt`
- Asserts presence of `btnBack`, `rvFavorites`, `btnAddFavorite`, `tvStatus`, `layoutSettingsLeft`, `layoutSettingsRight`.
- Asserts that `btnAddFavorite` has `layout_height >= 56dp`.

### Test 2: `SettingsLandscapeInteractionTest.kt`
- Tests adding a new favorite in `SettingsActivity`.
- Verifies setting `is_starred = true` un-stars previous starred items (single-starred invariant).

---

Next Phase: [Phase 05: Android 13 Automotive Integration & Verification](phase-05-android13-automotive-integration-and-tests.md)
