# Phase 02: CarActivity Supabase Failure & Wi-Fi Settings Trigger
Status: 🟢 Completed
Dependencies: [Phase 01: Network Permission & Error Detection Utility](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-01-network-permission-and-utility.md)

## Objective
Update `CarActivity.kt` (Landscape Car Mode) to detect when communication with the Supabase server fails (due to no internet, DNS resolution error, network timeout, or connection refusal), and automatically notify the driver and trigger the Wi-Fi settings/panel to allow immediate network reconnection.

## Requirements
### Functional
- [x] In `CarActivity.fetchCurrentLocation()` and startup fetch routines:
  - When Supabase API request fails with a connection or network error (`UnknownHostException`, `SocketTimeoutException`, `ConnectException`, or `IOException` detected via `NetworkUtils.isConnectionFailure(e)`):
    - Update `tvLocation.text` to display: `"⚠️ Không có kết nối mạng (Lỗi kết nối Supabase)"`.
    - Display Toast notification: `"Không thể kết nối đến máy chủ Supabase. Đang mở cài đặt Wi-Fi..."`.
    - Automatically call `NetworkUtils.openWifiSettings(this)`.
- [x] Guard against Infinite Loops:
  - Implement a state guard (e.g. `hasAutoOpenedWifiOnFailure`) or cooldown timestamp to prevent repeated auto-opening if the user dismisses settings and returns to the app without Wi-Fi.
- [x] Manual Reload Support:
  - When the user taps `btnReload` and the connection still fails, prompt the user with a Toast and allow launching Wi-Fi settings.
- [x] Dedicated Wi-Fi Button:
  - Keep `btnWifiSettings` fully functional for manual one-tap access at any time.
- [x] Isolation:
  - Ensure `PhoneActivity.kt` and `MainActivity.kt` remain strictly untouched.

### Non-Functional
- [x] Non-blocking async coroutines (`Dispatchers.IO` + `lifecycleScope`).
- [x] Clean UX on automotive landscape head units.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/locateshare/CarActivity.kt` - [Implement Supabase connection error detection & Wi-Fi settings auto-launcher]
- `app/src/test/java/com/skul9x/locateshare/CarActivitySupabaseConnectionFailureTest.kt` - [Unit test verifying connection error classification, auto-open trigger, and loop prevention guard]

## File-Based Test Criteria
- [x] `CarActivitySupabaseConnectionFailureTest.kt` tests that:
  - When a network `IOException` / `UnknownHostException` occurs on first fetch, the failure handler triggers the Wi-Fi settings intent and sets the guard flag to `true`.
  - On subsequent failed fetches with guard flag `true`, auto-opening is throttled to prevent infinite activity switching.
  - On successful Supabase response, no Wi-Fi settings intent is fired and the guard flag is reset.

---
Next Phase: [Phase 03: Unit & Integration Verification](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-03-unit-and-integration-verification.md)
