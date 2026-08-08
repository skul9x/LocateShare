# Plan: Automatic Wi-Fi Settings Launcher on Supabase Server Connection Failure in Car Mode
Created: 2026-08-08T07:52:00+07:00
Updated: 2026-08-08T07:54:30+07:00
Status: 🟡 Pending Review

## Overview
When LocateShare is running in **Chế độ Xe Hơi (Car Mode - CarActivity)** on the car display / landscape screen, the app will automatically detect if it cannot connect to the **Supabase server** (e.g., no internet connection, DNS resolution failure `UnknownHostException`, network timeout, or offline status). When connection failure is detected during data fetch or startup, the app displays a clear notification to the driver and automatically opens the **Wi-Fi settings** (or Android 10+ floating Internet Connectivity panel) so the driver can connect to Wi-Fi without manual menu navigation.

> [!NOTE]
> This behavior is strictly applied to **Car Mode (`CarActivity`)**. **Phone Mode (`PhoneActivity` / `MainActivity`)** is completely unaffected.

## Tech Stack & APIs
- **Platform**: Android (Kotlin, minSdk 24, targetSdk 35, compileSdk 35)
- **Target Screen**: `CarActivity` (Landscape Car Display Mode)
- **Backend / Database**: Supabase REST API (via Retrofit2 & OkHttp3)
- **Permissions**: `android.permission.ACCESS_NETWORK_STATE`, `android.permission.INTERNET`
- **Network & Error Detection**: 
  - `ConnectivityManager` with `NetworkCapabilities` (`NET_CAPABILITY_INTERNET`, `TRANSPORT_WIFI`)
  - Exception detection for `java.net.UnknownHostException`, `java.net.SocketTimeoutException`, `java.net.ConnectException`, and `java.io.IOException`
- **Settings Intents**: 
  - Android 10+ (API 29+): `android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY`
  - Fallback / Pre-Android 10: `android.provider.Settings.ACTION_WIFI_SETTINGS`
- **Testing**: JUnit 4 unit tests, file-based XML manifest assertions, intent routing tests, and Supabase connection failure handler tests

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | [Network Permission & Error Detection Utility](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-01-network-permission-and-utility.md) | ⬜ Pending | 0% |
| 02 | [CarActivity Supabase Failure & Wi-Fi Settings Trigger](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-02-caractivity-wifi-check-and-intent-trigger.md) | ⬜ Pending | 0% |
| 03 | [Unit & Integration Verification](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-03-unit-and-integration-verification.md) | ⬜ Pending | 0% |

## Quick Commands
- Run All Tests: `./gradlew test`
- Start Phase 1: `/code phase-01`
- Next Step: `/next`
