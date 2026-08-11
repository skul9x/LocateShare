# Phase 01: Automotive Design System & Theme Tokens

**Status:** ✅ Completed  
**Dependencies:** None  
**Target Platform:** Android 13 (API 33+) Automotive Landscape Displays  

---

## 🎯 Objective
Establish an automotive-grade design token system in XML resources, including high-contrast dark color palettes, glanceable typography tokens (sp), driver-safe touch target dimensions (dp >= 56dp/64dp), and rounded card backgrounds tailored for horizontal car screens.

---

## 📋 Requirements

### Functional Requirements
1. **Automotive Color Palette (`colors.xml`):**
   - Deep background: `#0F1015` (reduces glare during night driving).
   - Card surface: `#1B1D24` with subtle stroke `#2E3240`.
   - Accent & Navigation green: `#00E676` / `#2E7D32` with high luminous contrast.
   - Starred favorite gold/amber: `#FFD700` / `#FFA000`.
   - Primary text: `#FFFFFF`, Secondary text: `#B0B3C6`, Accent text: `#00E676`.
2. **Dimension Tokens (`dimens.xml` & `dimens-land.xml`):**
   - Minimum touch target height: `car_button_min_height = 56dp`.
   - Hero action button height: `car_hero_button_height = 68dp`.
   - Driver rail icon button size: `car_rail_icon_size = 56dp`.
   - Text sizes: `car_text_hero = 28sp`, `car_text_title = 22sp`, `car_text_body = 16sp`, `car_text_caption = 13sp`.
3. **Automotive Styles & Drawables (`styles.xml` / `themes.xml`):**
   - `Theme.LocateShare.CarLandscape` - High contrast, windowBackground `#0F1015`, noActionBar.
   - Custom drawables: `bg_car_card.xml`, `bg_car_hero_button.xml`, `bg_car_rail_item.xml`, `bg_car_badge.xml`.

### Non-Functional Requirements
- **Driver Safety:** High contrast ratio (WCAG AAA >= 7:1) for all critical navigation text.
- **Glanceability:** Legible from a 0.7m–1.0m driver viewing distance.

---

## 🛠 Implementation Steps

1. [x] **Step 1:** Add automotive color tokens to `app/src/main/res/values/colors.xml`.
2. [x] **Step 2:** Create `app/src/main/res/values-land/dimens.xml` containing automotive widescreen dimension tokens.
3. [x] **Step 3:** Define automotive card backgrounds and ripple effects in `app/src/main/res/drawable/`.
4. [x] **Step 4:** Define `Theme.LocateShare.CarLandscape` in `app/src/main/res/values/themes.xml`.
5. [x] **Step 5:** Create file-based unit/layout verification tests: `AutomotiveThemeXmlTest.kt` and `CarDimensionTokensTest.kt`.

---

## 📁 Files to Create/Modify
- `app/src/main/res/values/colors.xml` - [MODIFY] Automotive color palette
- `app/src/main/res/values-land/dimens.xml` - [NEW] Landscape car dimension tokens
- `app/src/main/res/drawable/bg_car_card.xml` - [NEW] Frosted dark card drawable with rounded corners
- `app/src/main/res/drawable/bg_car_hero_button.xml` - [NEW] Prominent green gradient navigation button drawable
- `app/src/main/res/drawable/bg_car_rail_item.xml` - [NEW] Left rail touch target background
- `app/src/main/res/values/themes.xml` - [MODIFY] Automotive landscape theme
- `app/src/test/java/com/skul9x/locateshare/layout/AutomotiveThemeXmlTest.kt` - [NEW] Theme test
- `app/src/test/java/com/skul9x/locateshare/layout/CarDimensionTokensTest.kt` - [NEW] Dimension validation test

---

## 🧪 Detailed File-Based Test Criteria

### Test 1: `AutomotiveThemeXmlTest.kt`
- Verifies that `colors.xml` defines all required automotive tokens: `car_bg_dark`, `car_card_surface`, `car_accent_green`, `car_text_primary`, `car_starred_gold`.
- Verifies contrast tokens exist and are correctly formatted hex colors.

### Test 2: `CarDimensionTokensTest.kt`
- Verifies that `dimens-land.xml` defines driver-safe touch target dimensions where `car_button_min_height >= 56dp` and `car_hero_button_height >= 64dp`.
- Verifies that typography tokens meet minimum readability thresholds (`car_text_hero >= 24sp`).

---

Next Phase: [Phase 02: Car Dashboard Landscape Layout](phase-02-car-dashboard-landscape-layout.md)
