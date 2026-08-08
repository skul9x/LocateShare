# Plan: Settings Screen Landscape Scroll & UI/UX Optimization
Created: 2026-08-08T08:18:00+07:00
Status: 🟡 In Progress

## Overview
Optimize the UI/UX of `SettingsActivity` to solve the non-scrollable issue in landscape orientation, add vertical scrollbars, and introduce a modern responsive 2-column layout for landscape/car screens.

## Problem Statement
1. `activity_settings.xml` currently uses a non-scrollable root `LinearLayout`.
2. In landscape mode (car head units / horizontal phones), the top "Add Favorite" form occupies the entire screen height, squishing the `RecyclerView` and preventing scrolling.
3. No vertical scrollbars are enabled (`android:scrollbars="vertical"`).

## Proposed Architecture & UX Improvements
1. **Portrait Layout (`res/layout/activity_settings.xml`)**:
   - Wrap the main content in `androidx.core.widget.NestedScrollView` with `android:fillViewport="true"` and `android:scrollbars="vertical"`.
   - Set `rvFavorites.isNestedScrollingEnabled = false` for smooth parent scrolling.
2. **Landscape Layout (`res/layout-land/activity_settings.xml`)**:
   - Dedicated 2-column layout:
     - Left column: Back button & "Add Favorite" input form.
     - Right column: "Favorites List" header & `RecyclerView` with `android:scrollbars="vertical"`.
3. **Automated File-Based Tests**:
   - `SettingsLayoutStructureTest.kt` verifying layout files, required view IDs, scroll containers, scrollbar attributes, and landscape 2-column configurations.

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Portrait Layout Scroll & Scrollbar Fix | ⬜ Pending | 0% |
| 02 | Landscape 2-Column Responsive Layout | ⬜ Pending | 0% |
| 03 | Layout Structure & Verification Unit Tests | ⬜ Pending | 0% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Run Tests: `./gradlew test --tests "com.skul9x.locateshare.SettingsLayoutStructureTest"`
