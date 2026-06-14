# Phase 02: Redirect Resolution Logic
Status: ✅ Completed
Dependencies: [Phase 01: Location Parser Utility](file:///D:/skul9x/LocateShare-main/plans/260614-0754-location-name-car-mode/phase-01-parser-utility.md)

## Objective
Implement `resolveRedirectUrlToName` in `LocationParser` to handle redirect resolution using OkHttpClient (via HTTP HEAD request) and extract the place name from the final redirected URL. Write a unit test using a real test URL or mock responses.

## Requirements
### Functional
- Intercept HTTP redirect codes (3xx) and follow them up to 5 times.
- Prevent OkHttpClient from automatically redirecting so we can inspect headers manually and check if any intermediate URL contains the `/place/` segment.
- Save bandwidth by using HTTP `HEAD` request instead of `GET` since we only need the response headers (`Location` header).
- Decode the redirect URL and extract the location name using `extractNameFromFullUrl`.

### Non-Functional
- Run as a suspend function on `Dispatchers.IO`.

## Implementation Steps
1. Add `resolveRedirectUrlToName` function in `LocationParser.kt`.
2. Add a unit test inside `LocationParserTest.kt` (or a separate `LocationRedirectTest.kt`) to test redirect resolution with a real known Google Maps short link or a mock server.

## Files to Create/Modify
- [MODIFY] [LocationParser.kt](file:///D:/skul9x/LocateShare-main/app/src/main/java/com/skul9x/locateshare/util/LocationParser.kt) - Implement `resolveRedirectUrlToName` method.
- [MODIFY] [LocationParserTest.kt](file:///D:/skul9x/LocateShare-main/app/src/test/java/com/skul9x/locateshare/util/LocationParserTest.kt) - Add tests for redirect resolution.

## Test Criteria (File-based Tests)
### Unit Test Case 1: Short Google Maps URL Redirect Resolution
- Input: `https://maps.app.goo.gl/SxdjHxg5vqmXecCk6?g_st=ac`
- Expected Output: `"Chi nhánh Công ty cổ phần Quốc tế Delta tại Bắc Ninh, KM 8+415, ĐT291, Quế Võ, Bắc Ninh"` (or empty if offline, but should work when connected).
- Assert that it does not block the thread and runs asynchronously.

---
Next Phase: [Phase 03: PhoneActivity Integration & E2E Testing](file:///D:/skul9x/LocateShare-main/plans/260614-0754-location-name-car-mode/phase-03-activity-integration.md)
