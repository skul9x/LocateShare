# Phase 03: PhoneActivity Integration & E2E Testing
Status: 🟩 Completed
Dependencies: [Phase 02: Redirect Resolution Logic](file:///D:/skul9x/LocateShare-main/plans/260614-0754-location-name-car-mode/phase-02-redirect-resolution.md)

## Objective
Integrate `LocationParser` inside `PhoneActivity` to extract the location name upon receiving a share intent, resolve URLs asynchronously via `resolveRedirectUrlToName` if needed, and send the location name alongside the URL to the Supabase endpoint. Run integration tests to verify successful end-to-end storage and retrieval of the location name.

## Requirements
### Functional
- In `PhoneActivity.kt`, update `handleIntent` to:
  - Extract url from shared text.
  - Parse name from shared text using `LocationParser.parseNameFromSharedText`.
  - If the parsed name is empty and the URL is a short link (`maps.app.goo.gl`), run `LocationParser.resolveRedirectUrlToName` in the coroutine scope.
  - Update status text and progress bar during parsing/resolution.
  - Invoke `sendLocationToSupabase(extractedUrl, extractedName)`.
- Update `sendLocationToSupabase` to accept and send the parsed `name` (instead of empty string `""`).

### Non-Functional
- Gracefully handle redirect resolution failures (e.g. timeout or no network) and default to sending empty name `""` or URL only.

## Implementation Steps
1. Modify `PhoneActivity.kt` to import `com.skul9x.locateshare.util.LocationParser`.
2. Update the logic inside `handleIntent` and `sendLocationToSupabase` as planned.
3. Update/run integration tests in `SupabaseIntegrationTest.kt` to confirm that the location name propagates correctly to the DB.

## Files to Create/Modify
- [MODIFY] [PhoneActivity.kt](file:///D:/skul9x/LocateShare-main/app/src/main/java/com/skul9x/locateshare/PhoneActivity.kt) - Integrate with `LocationParser` and update supabase send logic.
- [MODIFY] [SupabaseIntegrationTest.kt](file:///D:/skul9x/LocateShare-main/app/src/test/java/com/skul9x/locateshare/network/SupabaseIntegrationTest.kt) - Run/Assert that a location name is sent and matches when fetched.

## Test Criteria (File-based Tests)
### Integration Test Case: Supabase update and fetch location name
- Test code resides in [SupabaseIntegrationTest.kt](file:///D:/skul9x/LocateShare-main/app/src/test/java/com/skul9x/locateshare/network/SupabaseIntegrationTest.kt#L19-L37).
- Command: `./gradlew test --tests "com.skul9x.locateshare.network.SupabaseIntegrationTest"`
- Assertions:
  - `updateCurrentLocation` succeeds.
  - `getCurrentLocation` returns the exact name sent.
