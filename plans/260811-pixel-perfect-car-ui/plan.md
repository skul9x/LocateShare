# 🎨 LocateShare - Pixel-Perfect Car Landscape UI Redesign Plan

**Created:** 2026-08-11  
**Target Platform:** Android 13 (API Level 33+) / Automotive Head Units & Widescreen Displays  
**Aspect Ratio:** 16:9 / 21:9 Widescreen Landscape  
**Status:** ✅ Completed  

---

## 🎯 Executive Summary & Objective

Transform LocateShare's Android Car Landscape interface to be **100% visually pixel-perfect and identical** to the graphic design mockup (`car_screen_dashboard_1786421981628.png`):
1. **Left Driver Rail:** Deep obsidian background (`#101217`), rounded square icon tiles (`#1C202B`) with accent active states, high-contrast glow indicators for Cloud Sync and Starred Destinations.
2. **Center Primary Hero Card:** Floating dark card (`#161922`), pill-shaped sync status badge (`#132E22` + `#00E676`), large bold typography (`22sp–24sp`), and an oversized `72dp` high-contrast emerald green `"MỞ BẢN ĐỒ"` action button with adjacent reload tile.
3. **Right Quick Favorites Panel:** Elevated card items (`#1E222D`), gold star badges (`#FFD700`) vs accent pins (`#4F75FF`), and touch-safe navigation trigger chevrons.
4. **Frosted Modal & Settings Consistency:** Align Floating Favorites Modal and Settings Split-Screen with the same pixel-perfect design tokens.
5. **Automated Verification:** File-based unit tests for every phase to guarantee 100% passing tests with 0 regressions.

---

## 📋 Phase Breakdown

| Phase | Title | Scope | File-Based Tests | Status |
|---|---|---|---|---|
| **01** | [Visual Tokens & Custom Drawables](phase-01-visual-tokens-and-drawables.md) | Pixel-perfect colors, card corner radius tokens, glowing button drawables, pill badges, and icon tile backgrounds | `PixelPerfectDesignTokensTest.kt` | ✅ Completed |
| **02** | [Driver Rail & Hero Card Redesign](phase-02-driver-rail-and-hero-card-redesign.md) | Redesign `layout-land/activity_car.xml` Driver Rail (tile containers, spacing) & Hero Navigation Card (pill badge, 72dp green button) | `PixelPerfectCarDashboardLayoutTest.kt` | ✅ Completed |
| **03** | [Quick Favorites Item Redesign](phase-03-quick-favorites-card-item-redesign.md) | Redesign `item_quick_favorite.xml` & `QuickFavoriteAdapter` with elevated tile surfaces, status badges, and typography | `PixelPerfectQuickFavoritesItemTest.kt` | ✅ Completed |
| **04** | [Modal & Settings Polish](phase-04-favorites-modal-and-settings-polish.md) | Polish `dialog_favorites_card_popup.xml`, `item_favorite_card.xml`, and `activity_settings.xml` with matching aesthetics | `PixelPerfectModalAndSettingsTest.kt` | ✅ Completed |
| **05** | [Full E2E Suite & APK Verification](phase-05-full-e2e-suite-and-apk-verification.md) | Execute 140+ test suite, build debug APK, and verify on connected device | `PixelPerfectE2EIntegrationTest.kt` | ✅ Completed |

---

## 🧪 Comprehensive Verification Strategy

- **File-Based Testing:** Each phase includes a dedicated JUnit/XML parsing test that validates layout hierarchy, view IDs, dimension tokens, and background drawables.
- **Android Automotive Compliance:** Touch target heights >= 56dp/72dp, driver distraction optimized tagging, and edge-to-edge system insets support.
- **Zero Regression:** Preserves Supabase sync, location parsing, double-tap gestures, and phone mode features.
