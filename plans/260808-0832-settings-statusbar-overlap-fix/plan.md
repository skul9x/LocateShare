# Plan: Fix Status Bar Overlap on Settings Screen
Created: 2026-08-08T08:32:00+07:00
Status: ✅ Complete

## Overview
Fix the status bar overlapping content issue in `SettingsActivity` for both portrait (`res/layout/activity_settings.xml`) and landscape (`res/layout-land/activity_settings.xml`) layouts by configuring `android:fitsSystemWindows="true"` on root layout containers, and provide comprehensive file-based automated tests for validation.

## Problem Statement
1. The app uses `Theme.Material3.DayNight.NoActionBar`.
2. `activity_settings.xml` and `res/layout-land/activity_settings.xml` lack `android:fitsSystemWindows="true"` on their root layout elements.
3. System status bar (battery, clock, wifi icons) overlays the top toolbar containing `btnBackSettings` ("⬅️ Quay lại") and the "⚙️ Cài đặt" title.

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Apply `fitsSystemWindows="true"` to Portrait and Landscape Layouts | ✅ Complete | 100% |
| 02 | Implement File-Based XML Verification Tests & Test Execution | ✅ Complete | 100% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Run Tests: `./gradlew testDebugUnitTest --tests "com.skul9x.locateshare.layout.SettingsStatusbarInsetsTest"`
