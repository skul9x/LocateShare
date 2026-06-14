# Phase 01: Location Parser Utility
Status: ✅ Completed
Dependencies: None

## Objective
Extract parsing helper logic from `PhoneActivity` into a standalone, testable `LocationParser` utility class and write comprehensive unit tests.

## Requirements
### Functional
- Parse location name from multiline shared text, prioritizing the first line.
- Correctly handle generic labels ("Đã ghim", "dropped pin", etc.) by appending the address details from the second line (e.g. `Đã ghim (Gần Hải Châu, Đà Nẵng)`).
- Handle URLs shared without names by returning an empty name.
- Correctly decode full Google Maps URLs containing `/place/Name` path segment to extract and decode the name.

### Non-Functional
- No Android framework dependencies in the core parsing functions so they can be unit-tested on JVM.

## Implementation Steps
1. Create `app/src/main/java/com/skul9x/locateshare/util/LocationParser.kt`.
2. Implement `parseNameFromSharedText` and `extractNameFromFullUrl` functions in `LocationParser`.
3. Create `app/src/test/java/com/skul9x/locateshare/util/LocationParserTest.kt`.
4. Implement comprehensive unit test cases in `LocationParserTest.kt`.

## Files to Create/Modify
- [NEW] [LocationParser.kt](file:///D:/skul9x/LocateShare-main/app/src/main/java/com/skul9x/locateshare/util/LocationParser.kt) - Houses the static extraction and parsing helper functions.
- [NEW] [LocationParserTest.kt](file:///D:/skul9x/LocateShare-main/app/src/test/java/com/skul9x/locateshare/util/LocationParserTest.kt) - Unit test class containing multiple test cases.

## Test Criteria (File-based Tests)
### Unit Test Case 1: Specific named location
- Input:
  ```text
  Cộng Cà Phê
  101 Hoàng Sa, Đa Kao, Quận 1, Thành phố Hồ Chí Minh, Vietnam
  https://maps.app.goo.gl/xxxxx
  ```
- Output: `"Cộng Cà Phê"`

### Unit Test Case 2: Pinned location (Generic label)
- Input:
  ```text
  Đã ghim
  Gần Hải Châu, Đà Nẵng
  https://maps.app.goo.gl/xxxxx
  ```
- Output: `"Đã ghim (Gần Hải Châu, Đà Nẵng)"`

### Unit Test Case 3: URL only (No text/name)
- Input: `"https://maps.app.goo.gl/xxxxx"`
- Output: `""`

### Unit Test Case 4: Decode URL place name
- Input: `"https://www.google.com/maps/place/Chi+nh%C3%A1nh+C%C3%B4ng+ty+c%E1%BB%95+ph%E1%BA%A7n+Qu%E1%BB%91c+t%E1%BA%BF+Delta+t%E1%BA%A1i+B%E1%BA%AFc+Ninh,+KM+8%2B415,+%C4%90T291,+Qu%E1%BA%BF+V%C3%B5,+B%E1%BA%AFc+Ninh/data=!4m2!3m1!..."`
- Output: `"Chi nhánh Công ty cổ phần Quốc tế Delta tại Bắc Ninh, KM 8+415, ĐT291, Quế Võ, Bắc Ninh"`

---
Next Phase: [Phase 02: Redirect Resolution Logic](file:///D:/skul9x/LocateShare-main/plans/260614-0754-location-name-car-mode/phase-02-redirect-resolution.md)
