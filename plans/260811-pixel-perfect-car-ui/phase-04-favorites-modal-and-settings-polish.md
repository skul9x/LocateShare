# Phase 04: Modal & Settings Polish

**Status:** ✅ Completed  
**Dependencies:** Phase 01, Phase 02, Phase 03  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Apply the pixel-perfect design system to the Floating Favorites Modal (`dialog_favorites_card_popup.xml`, `item_favorite_card.xml`) and the Split-Screen Settings Activity (`layout-land/activity_settings.xml`):
1. **Floating Favorites Modal:**
   - 85% background dim + blur behind effect (`FLAG_BLUR_BEHIND`).
   - 2-column grid cards with dark surface (`#1E222D`), gold/accent badges, and direct navigation buttons.
2. **Settings Split-Screen Activity:**
   - Left Master Pane (`35%` width): Preferences, Supabase live connection badge, and default location summary.
   - Right Detail Pane (`65%` width): Add Favorite card form and interactive favorites management list.

---

## 📋 Requirements

### Functional Requirements
1. Maintain all view IDs in `dialog_favorites_card_popup.xml` and `activity_settings.xml`.
2. Ensure touch-safe minimums across all inputs, dialogs, and action buttons.
3. Preserve CRUD operations (Add, Edit, Delete, Toggle Star) without regression.

### Non-Functional Requirements
- High-contrast visual polish matching the primary dashboard aesthetic.
- 100% test coverage across modal dialog and settings split-screen layouts.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Update `dialog_favorites_card_popup.xml` and `item_favorite_card.xml` with pixel-perfect tokens.
2. [x] **Step 2:** Polish `res/layout-land/activity_settings.xml` with matching dark surface cards and typography.
3. [x] **Step 3:** Create and execute file-based verification test `PixelPerfectModalAndSettingsTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout-land/dialog_favorites_card_popup.xml` - [MODIFY] Polish floating modal layout
- `app/src/main/res/layout/item_favorite_card.xml` - [MODIFY] Polish popup card item
- `app/src/main/res/layout-land/activity_settings.xml` - [MODIFY] Polish landscape settings layout
- `app/src/test/java/com/skul9x/locateshare/layout/PixelPerfectModalAndSettingsTest.kt` - [NEW] File-based verification test

---

## 🧪 Detailed File-Based Test Criteria

### Test: `PixelPerfectModalAndSettingsTest.kt`
- Verifies view IDs and layout constraints in `dialog_favorites_card_popup.xml`.
- Verifies master-detail 35/65 weight distribution in `layout-land/activity_settings.xml`.
- Verifies touch target sizes meet automotive guidelines.
