# Phase 01: Network Permission & Error Detection Utility
Status: ✅ Completed
Dependencies: None

## Objective
Configure required network permissions in `AndroidManifest.xml` and create a comprehensive network and connection error detection utility `NetworkUtils.kt` in `com.skul9x.locateshare.util` that handles network state checks, Supabase connection failure classification, and backward-compatible Wi-Fi settings intent generation.

## Requirements
### Functional
- [x] Add `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />` to `app/src/main/AndroidManifest.xml`.
- [x] Implement `NetworkUtils.isNetworkConnected(context: Context): Boolean`:
  - Uses modern `ConnectivityManager` and `NetworkCapabilities` to check `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED`.
- [x] Implement `NetworkUtils.isWifiConnected(context: Context): Boolean`:
  - Checks if active network transport is `NetworkCapabilities.TRANSPORT_WIFI`.
- [x] Implement `NetworkUtils.isConnectionFailure(throwable: Throwable): Boolean`:
  - Returns `true` if the exception is an `UnknownHostException` (DNS resolution failed, no internet), `SocketTimeoutException`, `ConnectException`, or general `IOException` originating from Supabase/Retrofit network transport.
- [x] Implement `NetworkUtils.getWifiSettingsIntent(context: Context): Intent`:
  - Returns `Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)` on Android 10+ (API level >= 29).
  - Returns `Intent(Settings.ACTION_WIFI_SETTINGS)` on Android 9 and below.
- [x] Implement `NetworkUtils.openWifiSettings(context: Context): Boolean`:
  - Safely launches Wi-Fi settings with `ActivityNotFoundException` fallback handling.

### Non-Functional
- [x] Safe execution from any thread.
- [x] Zero deprecated APIs (avoids deprecated `NetworkInfo`).
- [x] Full compatibility with Kotlin Coroutines and Retrofit error handling.

## Files to Create/Modify
- `app/src/main/AndroidManifest.xml` - [Add ACCESS_NETWORK_STATE permission]
- `app/src/main/java/com/skul9x/locateshare/util/NetworkUtils.kt` - [Network state, Supabase error classification & settings intent utility]
- `app/src/test/java/com/skul9x/locateshare/util/ManifestNetworkPermissionTest.kt` - [Test ensuring manifest contains ACCESS_NETWORK_STATE]
- `app/src/test/java/com/skul9x/locateshare/util/NetworkUtilsTest.kt` - [Unit tests for connection error classification and intent generation]

## File-Based Test Criteria
- [x] `ManifestNetworkPermissionTest.kt` verifies that `ACCESS_NETWORK_STATE` is present in `AndroidManifest.xml`.
- [x] `NetworkUtilsTest.kt` asserts that:
  - `UnknownHostException("Unable to resolve host \"supabase.co\"")` evaluates to `isConnectionFailure = true`.
  - `SocketTimeoutException("timeout")` evaluates to `isConnectionFailure = true`.
  - Non-network exceptions (e.g. `IllegalStateException`, `NullPointerException`) evaluate to `false`.
  - Settings intent maps correctly across Android API versions.

---
Next Phase: [Phase 02: CarActivity Supabase Failure & Wi-Fi Settings Trigger](file:///home/skul9x/Desktop/Test_code/LocateShare-main/plans/260808-0752-wifi-auto-connect-check/phase-02-caractivity-wifi-check-and-intent-trigger.md)

