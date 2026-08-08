# Phase 01: Status Bar Insets & `fitsSystemWindows` Fix
Status: ✅ Complete
Dependencies: None

## Objective
Prevent system status bar overlap on the Settings screen by applying `android:fitsSystemWindows="true"` to the root layout elements in both portrait and landscape layout XML files.

## Requirements
### Functional
- [x] In `app/src/main/res/layout/activity_settings.xml`, add `android:fitsSystemWindows="true"` to the root `androidx.core.widget.NestedScrollView`.
- [x] In `app/src/main/res/layout-land/activity_settings.xml`, add `android:fitsSystemWindows="true"` to the root `LinearLayout`.
- [x] Ensure content padding and visual background remain intact with dark theme `#121212`.

### Non-Functional
- [x] Maintain consistent edge-to-edge window inset handling aligned with `activity_car.xml`.
- [x] No regression on nested scrolling or button click interactions.

## Implementation Steps
1. [x] Update `app/src/main/res/layout/activity_settings.xml` to include `android:fitsSystemWindows="true"`.
2. [x] Update `app/src/main/res/layout-land/activity_settings.xml` to include `android:fitsSystemWindows="true"`.
3. [x] Verify XML syntax and attribute namespaces.

## Files to Create/Modify
- `app/src/main/res/layout/activity_settings.xml` - Add `android:fitsSystemWindows="true"` to root `NestedScrollView`.
- `app/src/main/res/layout-land/activity_settings.xml` - Add `android:fitsSystemWindows="true"` to root `LinearLayout`.

## Test Criteria
- [x] DOM inspection confirms `android:fitsSystemWindows="true"` on portrait root element.
- [x] DOM inspection confirms `android:fitsSystemWindows="true"` on landscape root element.
- [x] Visual verification confirms status bar area is appropriately padded and does not overlap `btnBackSettings` or `⚙️ Cài đặt`.

## Notes
- `activity_car.xml` already sets `android:fitsSystemWindows="true"` as the reference pattern in this codebase.

---
Next Phase: [Phase 02: File-Based Unit Tests](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0832-settings-statusbar-overlap-fix/phase-02-file-based-unit-tests.md)
