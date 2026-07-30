# Phase 02: Activity Intent Logic & WindowInsets Edge-to-Edge Handler [COMPLETED]

## Objective
Implement the logic in `CarActivity.kt` to launch Android device Wi-Fi settings when the Wi-Fi icon button is tapped, and register a dynamic `WindowInsets` listener to prevent status bar overlap on modern Android versions (Android 15+ Edge-to-Edge enforcement).

## Requirements
- Bind `btnWifiSettings` `ImageButton` in `CarActivity.kt`.
- Implement `openWifiSettings()` function using standard Android system intent:
  `Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)`.
- Wrap intent invocation in a `try-catch` block to handle device edge cases or missing activity handlers gracefully.
- Attach `ViewCompat.setOnApplyWindowInsetsListener` to `rootCarLayout` in `onCreate()` to dynamically add status bar and cutout top insets padding.

## Implementation Steps
1. **Import Requirements**: Import `android.provider.Settings`, `androidx.core.view.ViewCompat`, and `androidx.core.view.WindowInsetsCompat`.
2. **Apply WindowInsets Padding**:
```kotlin
val rootLayout = findViewById<ConstraintLayout>(R.id.rootCarLayout)
ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
    val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    view.setPadding(
        statusBarInsets.left,
        statusBarInsets.top,
        statusBarInsets.right,
        statusBarInsets.bottom
    )
    insets
}
```
3. **Bind View & OnClickListener**:
```kotlin
val btnWifiSettings = findViewById<ImageButton>(R.id.btnWifiSettings)
btnWifiSettings.setOnClickListener { openWifiSettings() }
```
4. **Implement Helper Method**:
```kotlin
private fun openWifiSettings() {
    try {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "Không thể mở cài đặt Wi-Fi", Toast.LENGTH_SHORT).show()
    }
}
```

## Files to Create/Modify
- `[MODIFY]` [CarActivity.kt](file:///home/skul9x/Desktop/Test_code/LocateShare-main/app/src/main/java/com/skul9x/locateshare/CarActivity.kt)

## File-Based Phase Verification Test
Verify implementation logic via component test in `app/src/test/java/com/skul9x/locateshare/util/WifiSettingsIntentTest.kt` verifying:
- Target intent action string matches `android.settings.WIFI_SETTINGS`.
- Exception handling contract returns structured fallback Toast.
