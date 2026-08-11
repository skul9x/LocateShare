# Phase 01: Visual Tokens & Custom Drawables

**Status:** ✅ Completed  
**Dependencies:** None  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Define and implement all color tokens, dimension values, and custom shape drawables required to achieve an identical match with the design mockup (`car_screen_dashboard_1786421981628.png`):
1. **Palette Tokens:** Deep obsidian backgrounds (`#101217`, `#141720`), card surface elevated tones (`#181B24`, `#1E222D`), emerald action greens (`#00D26A`, `#00E676`, `#132E22`), and gold accent (`#FFD700`).
2. **Dimension Tokens:** Corner radiuses (`16dp`, `20dp`, `24dp`), Hero button height (`72dp`), Driver Rail width (`88dp`), and touch target minimums (`56dp`).
3. **Custom Drawables:**
   - `bg_car_rail_tile.xml`: Rounded square icon background (`#1C202B` with `#282D3C` stroke).
   - `bg_car_pill_badge.xml`: Rounded pill sync indicator background with green accent border.
   - `bg_car_btn_navigation.xml`: Vibrant emerald green button background with smooth rounded corners (`20dp`).
   - `bg_car_btn_reload.xml`: Dark elevated button background (`#222632`) for the reload action.

---

## 📋 Requirements

### Functional Requirements
1. Define all design tokens in `res/values/colors.xml` and `res/values/dimens.xml`.
2. Implement vector shape drawables in `res/drawable/` for rail tiles, pill badge, and navigation buttons.
3. Ensure backwards compatibility with Android 7.0+ (API 24+) while optimizing for Android 13+.

### Non-Functional Requirements
- High contrast compliant with automotive driver glanceability guidelines (WCAG AAA contrast ratios).
- Zero regression on existing theme and dialog styles.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Update `res/values/colors.xml` with pixel-perfect color constants.
2. [x] **Step 2:** Update `res/values/dimens.xml` with card paddings, corner radiuses, and button heights (`72dp`).
3. [x] **Step 3:** Create drawables: `bg_car_rail_tile.xml`, `bg_car_pill_badge.xml`, `bg_car_btn_navigation.xml`, and `bg_car_btn_reload.xml`.
4. [x] **Step 4:** Create and execute file-based verification test `PixelPerfectDesignTokensTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/values/colors.xml` - [MODIFY] Add mockup palette tokens
- `app/src/main/res/values/dimens.xml` - [MODIFY] Add mockup dimension tokens
- `app/src/main/res/drawable/bg_car_rail_tile.xml` - [NEW] Driver rail icon tile drawable
- `app/src/main/res/drawable/bg_car_pill_badge.xml` - [NEW] Sync header pill badge drawable
- `app/src/main/res/drawable/bg_car_btn_navigation.xml` - [NEW] Hero button emerald background
- `app/src/main/res/drawable/bg_car_btn_reload.xml` - [NEW] Reload square button drawable
- `app/src/test/java/com/skul9x/locateshare/layout/PixelPerfectDesignTokensTest.kt` - [NEW] File-based verification test

---

## 🧪 Detailed File-Based Test Criteria

### Test: `PixelPerfectDesignTokensTest.kt`
- Verifies that all required color tokens exist in `colors.xml` (`car_bg_obsidian`, `car_card_surface_dark`, `car_accent_emerald`, `car_pill_badge_bg`, `car_pill_badge_stroke`, etc.).
- Verifies that all required dimension tokens exist in `dimens.xml` (`car_hero_button_height_large` = `72dp`, `car_card_radius_large` = `20dp`, etc.).
- Verifies that all custom drawables exist, are valid XML shapes, and define proper corner radii and stroke widths.
