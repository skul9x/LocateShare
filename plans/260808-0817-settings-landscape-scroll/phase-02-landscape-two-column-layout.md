# Phase 02: Landscape 2-Column Responsive Layout
Status: ✅ Completed
Dependencies: [Phase 01](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0817-settings-landscape-scroll/phase-01-portrait-scroll-and-scrollbar.md)

## Objective
Create a dedicated landscape layout file (`app/src/main/res/layout-land/activity_settings.xml`) utilizing a modern 2-column split layout optimized for car head units and horizontal displays.

## Requirements
### Functional
- [x] Create layout resource directory `app/src/main/res/layout-land/` if not present.
- [x] Create `activity_settings.xml` in `layout-land` with a 2-column layout (Horizontal LinearLayout or ConstraintLayout):
  - **Left Column**: Top Bar + "Add Favorite" input form wrapped inside a `NestedScrollView` / `ScrollView` with `android:scrollbars="vertical"`.
  - **Right Column**: "Favorites List" header + `RecyclerView` (`@id/rvFavorites`) with `android:scrollbars="vertical"`.
- [x] Keep identical View IDs: `btnBackSettings`, `etFavName`, `etFavUrl`, `btnAddFavorite`, `rvFavorites`.

### Non-Functional / UX
- [x] Optimal width allocation (e.g., 40-45% for form column, 55-60% for list column).
- [x] Both columns scroll independently if needed, preventing keyboard or form inputs from hiding the list.
- [x] Vertical scrollbar indicators for visual clarity.

## Implementation Steps
1. [x] Create directory `app/src/main/res/layout-land`.
2. [x] Create `app/src/main/res/layout-land/activity_settings.xml` with 2-column split design.
3. [x] Run file-based validation test for Phase 02.

## Files to Create/Modify
- `app/src/main/res/layout-land/activity_settings.xml` - [NEW]

## File-Based Verification Test
- **Test File**: `app/src/test/java/com/skul9x/locateshare/layout/SettingsLandscapeLayoutTest.kt`
- **Test Criteria**:
  - [x] Validates `activity_settings.xml` exists in `app/src/main/res/layout-land/`.
  - [x] Parses XML and verifies presence of all required view IDs (`btnBackSettings`, `etFavName`, `etFavUrl`, `btnAddFavorite`, `rvFavorites`).
  - [x] Verifies `android:scrollbars="vertical"` is present on `rvFavorites` or scroll containers in landscape layout.
  - [x] Asserts horizontal splitting (e.g., `android:orientation="horizontal"` or side-by-side constraints) is utilized.

---
Next Phase: [phase-03-layout-and-interaction-tests.md](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0817-settings-landscape-scroll/phase-03-layout-and-interaction-tests.md)
