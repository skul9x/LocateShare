# Phase 03: Quick Favorites Item Redesign

**Status:** ✅ Completed  
**Dependencies:** Phase 01, Phase 02  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Redesign the Right Quick Favorites Panel in `activity_car.xml` and item layout `item_quick_favorite.xml` to match the exact visual style of the mockup:
1. **Header Row:** Gold title `⭐ ĐỊA ĐIỂM ƯA THÍCH` with a styled `TẤT CẢ ↗` button.
2. **Item Card (`item_quick_favorite.xml`):**
   - Elevated dark tile background (`#1E222D`), corner radius `14dp`, height `68dp`.
   - Left indicator: Amber star icon for starred favorite item; vibrant location pin for other items.
   - Text layout: Bold white title (`16sp`, `#FFFFFF`), secondary address/url (`12sp`, `#8E95A5`), single line with ellipsis.
   - Right action: Touch-safe navigation chevron icon (`ic_navigation` or `ic_arrow_forward`).
3. **Adapter Binding (`QuickFavoriteAdapter.kt`):**
   - Bind click listeners with immediate navigation callback.
   - Smooth list update animations.

---

## 📋 Requirements

### Functional Requirements
1. Maintain view IDs (`cardQuickFavItem`, `ivStarBadge`, `tvQuickFavName`, `tvQuickFavAddress`, `ivQuickFavNav`).
2. Correctly display starred items with gold star badge and regular favorites with location pin icon.
3. Keep single-tap direct navigation trigger on favorite item clicks.

### Non-Functional Requirements
- Minimum item touch target height `64dp` (complies with Android for Cars guidelines).
- No UI stuttering during list scroll or update.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Redesign `app/src/main/res/layout/item_quick_favorite.xml` and `res/layout-land/item_quick_favorite.xml`.
2. [x] **Step 2:** Update `QuickFavoriteAdapter.kt` for star badge vs regular favorite icon toggling.
3. [x] **Step 3:** Create and execute file-based verification test `PixelPerfectQuickFavoritesItemTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/layout/item_quick_favorite.xml` - [MODIFY] Redesign favorite item tile
- `app/src/main/java/com/skul9x/locateshare/adapter/QuickFavoriteAdapter.kt` - [MODIFY] Icon binding updates
- `app/src/test/java/com/skul9x/locateshare/layout/PixelPerfectQuickFavoritesItemTest.kt` - [NEW] File-based verification test

---

## 🧪 Detailed File-Based Test Criteria

### Test: `PixelPerfectQuickFavoritesItemTest.kt`
- Verifies that `item_quick_favorite.xml` contains all required view IDs (`cardQuickFavItem`, `ivStarBadge`, `tvQuickFavName`, `tvQuickFavAddress`, `ivQuickFavNav`).
- Verifies card corner radius is `14dp` and item height >= `64dp`.
- Verifies adapter binds title, address, and star status correctly for both starred and non-starred items.
