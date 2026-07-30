# Phase 01: UI Vector Icon, Layout & Status Bar Insets Config [COMPLETED]

## Objective
Design and add a Wi-Fi icon button to the top-right header of `activity_car.xml`, and configure root layout attributes (`android:fitsSystemWindows="true"` & `android:id="@+id/rootCarLayout"`) to prevent system status bar overlap.

## Requirements
- Create clean vector drawable `ic_wifi.xml` in `app/src/main/res/drawable/`.
- Add an `ImageButton` with `id="@+id/btnWifiSettings"` in `activity_car.xml`.
- Position the button in the top action bar next to `btnSettings`.
- Maintain consistent button dimensions (48dp x 48dp), white tint (`#FFFFFF`), and borderless ripple feedback (`?attr/selectableItemBackgroundBorderless`).
- Set `android:id="@+id/rootCarLayout"` and `android:fitsSystemWindows="true"` on the root `ConstraintLayout` to prevent system status bar overlap.
- Add appropriate `contentDescription` for accessibility.

## Implementation Steps
1. **Create Vector Icon**: Add `ic_wifi.xml` vector resource depicting a Wi-Fi signal icon.
2. **Update Layout Root**: Modify `activity_car.xml` root `ConstraintLayout` to add `android:id="@+id/rootCarLayout"` and `android:fitsSystemWindows="true"`.
3. **Add Wi-Fi Button**: Position `btnWifiSettings` constraint-aligned between `tvTitle` and `btnSettings`.
4. **Verify Constraints**: Ensure no layout overlap across different screen resolutions and landscape orientations.

## Files to Create/Modify
- `[NEW]` [ic_wifi.xml](file:///home/skul9x/Desktop/Test_code/LocateShare-main/app/src/main/res/drawable/ic_wifi.xml)
- `[MODIFY]` [activity_car.xml](file:///home/skul9x/Desktop/Test_code/LocateShare-main/app/src/main/res/layout/activity_car.xml)

## File-Based Phase Verification Test
Create test script in `app/src/test/java/com/skul9x/locateshare/util/CarLayoutXmlTest.kt` to parse `activity_car.xml` and verify:
- Presence of `btnWifiSettings` element.
- Presence of `rootCarLayout` ID on root element.
- Presence of `fitsSystemWindows="true"` property.
- Correct `android:src` drawable reference (`@drawable/ic_wifi`).
