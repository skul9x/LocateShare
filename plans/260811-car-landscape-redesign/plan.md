# 🚗 LocateShare - Android 13 Car Screen Landscape Redesign Plan

**Created:** 2026-08-11  
**Target Platform:** Android 13 (API Level 33+) / Automotive Head Units & Widescreen In-Car Displays  
**Aspect Ratio:** 16:9 / 21:9 Widescreen Landscape  
**Status:** 🟡 Ready for Review  

---

## 🎯 Executive Summary & Design Vision

**LocateShare** is designed to bridge Google Maps locations shared from a mobile phone to an in-car Android head unit via Supabase real-time cloud sync.

This redesign completely overhauls LocateShare for **Android 13 in-car landscape displays** (automotive head units, horizontal car infotainment screens, and dashboard-mounted devices), following the **Android for Cars Design Guidelines (Driver Distraction & Glanceability Standards)**:
1. **Driver-Centric Ergonomics (Left-Hand Rail):** Critical driver controls (Back, Cloud Sync Status, Wi-Fi Dialog Trigger, Starred Fav quick action, Settings) are docked on the left rail closest to the driver.
2. **Glanceable Hierarchy (2-Second Rule):** High-contrast typography (24sp–32sp), clean destination badges, and an oversized primary **"MỞ BẢN ĐỒ / START NAVIGATION"** action button (minimum touch target height 64dp+).
3. **Split-Screen & Multi-Column Layouts:** Utilizing widescreen 16:9 / 21:9 real estate with a central Synced Location Card and a right-hand Quick Favorites / Recent Destinations panel.
4. **85% Frosted Glassmorphism Floating Modal:** Enhanced Double-tap Floating Favorites modal with `FLAG_BLUR_BEHIND` (API 31+) and `dimAmount = 0.85f` with a 2-column driver-safe touch grid.
5. **System Insets & Automotive Polish:** Immersive full-screen support, Android 13 window insets handling, and night/dark high-contrast palette.

---

## 🎨 UI Mockups & Visual Architecture

### 1. Main In-Car Landscape Dashboard
- **Left Driver Rail:** Fast access to Back, Sync status indicator, Network health, and Settings.
- **Center Hero Card:** Real-time synced location name, cleaned address, sync time indicator, and giant prominent navigation trigger button.
- **Right Panel:** Direct-tap Quick Favorites tiles (Home ⭐, Office, Cafe, etc.).

### 2. Double-Tap Floating Favorites Modal (85% Blur)
- 2-column grid modal with high-contrast `#1E1E1E` frosted glass card styling.
- 60dp+ touch targets, single-tap direct navigation trigger.

### 3. Split-Screen Settings & Places Management
- Left column: Preferences, auto-sync toggles, Supabase connection status.
- Right column: Interactive list for adding, editing, deleting, and starring favorite destinations.

---

## 📋 Phases Breakdown

| Phase | Title | Scope | File-Based Tests | Status |
|---|---|---|---|---|
| **01** | [Design System & Theme Tokens](phase-01-design-system-and-theme.md) | Automotive color palette, touch-target dimensions (>=56dp/64dp), glanceable typography tokens, high-contrast dark theme | `AutomotiveThemeXmlTest.kt`, `CarDimensionTokensTest.kt` | ✅ Completed |
| **02** | [Car Dashboard Landscape Layout](phase-02-car-dashboard-landscape-layout.md) | Redesign `layout-land/activity_car.xml` with Left Driver Rail, Hero Navigation Card, and Right Quick-Favorites panel | `CarLandscapeLayoutXmlTest.kt`, `CarDashboardComponentTest.kt` | ✅ Completed |
| **03** | [Favorites Floating Modal Landscape](phase-03-favorites-floating-modal-landscape.md) | Redesign `dialog_favorites_card_popup.xml` and `item_favorite_card.xml` into a 2-column widescreen floating modal with 85% background blur | `FavoritesFloatingModalLandscapeTest.kt`, `FavoriteCardAdapterLandscapeTest.kt` | ✅ Completed |
| **04** | [Settings Split-Screen Landscape](phase-04-settings-split-screen-landscape.md) | Redesign `layout-land/activity_settings.xml` with two-pane layout: Preferences & Cloud status (left) + Favorites CRUD list (right) | `SettingsLandscapeLayoutXmlTest.kt`, `SettingsLandscapeInteractionTest.kt` | ✅ Completed |
| **05** | [Android 13 Automotive Integration & Verification](phase-05-android13-automotive-integration-and-tests.md) | Android 13 Window Insets API, Double-tap vibration tolerance, Car UX distraction tags, end-to-end test suite execution | `CarGestureAndConnectivitySuiteTest.kt`, `AutomotiveLandscapeE2ETest.kt` | ✅ Completed |

---

## 🧪 Comprehensive Verification Strategy

Each phase includes automated file-based JUnit/Robolectric layout tests:
- **XML Structure Verification:** Verifies view IDs, layout constraints, weights, and minimum touch target dimensions.
- **Orientation & Insets Compatibility:** Ensures elements adapt seamlessly across standard (1280x720, 1920x1080) and ultra-wide (1920x720) car screen resolutions.
- **Component & Gesture Logic Tests:** Validates `DoubleTapHandler` timing, single vs double tap disambiguation, and adapter data binding without regressions.

---

## 🚀 Execution Commands
- Execute Phase 1: `/code phase-01`
- Review Plan & Specs: `implementation_plan.md`
