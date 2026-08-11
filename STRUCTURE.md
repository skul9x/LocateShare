# 📁 LocateShare Project Structure & Component Architecture

Tài liệu này mô tả chi tiết toàn bộ cấu trúc mã nguồn, thành phần hệ thống, mối liên kết giữa các class, tài nguyên UI, và bộ kiểm thử (Test Suite) của dự án **LocateShare**.

---

## 🏗️ 1. Cấu trúc Cây Thư mục Dự án (Project File Tree)

```
LocateShare-main/
├── README.md                                   # Tài liệu giới thiệu & Hướng dẫn sử dụng chính
├── STRUCTURE.md                                # Tài liệu chi tiết kiến trúc & cấu trúc mã nguồn
├── build.gradle.kts                            # Gradle configuration cấp Root
├── settings.gradle.kts                         # Cấu hình Module Gradle
│
├── app/
│   ├── build.gradle.kts                        # Cấu hình dependencies, SDK version (Min: 24, Target/Compile: 35)
│   ├── proguard-rules.pro                      # Quy tắc tối ưu và bảo vệ code khi release
│   │
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml             # Khai báo Permissions, Activities, Intent Filters
│       │   │
│       │   ├── java/com/skul9x/locateshare/
│       │   │   ├── MainActivity.kt             # Màn hình khởi đầu: Điều hướng chế độ Xe hoặc Điện thoại
│       │   │   ├── PhoneActivity.kt            # Giao diện Gửi: Nhận Share Intent từ Maps & gửi Supabase
│       │   │   ├── CarActivity.kt              # Giao diện Xe: Nhận vị trí, Double-tap Floating Card Popup (85% Blur), Auto Wi-Fi dismiss
│       │   │   ├── SettingsActivity.kt         # Giao diện Cài đặt: Quản lý danh sách Yêu thích (Favorites)
│       │   │   │
│       │   │   ├── adapter/
│       │   │   │   ├── FavoriteAdapter.kt      # RecyclerView Adapter danh sách địa điểm yêu thích (SettingsActivity)
│       │   │   │   └── FavoriteCardAdapter.kt  # RecyclerView Adapter thẻ xe hơi ô tô (CarActivity Floating Popup)
│       │   │   │
│       │   │   ├── network/
│       │   │   │   ├── ApiService.kt           # Interface Retrofit định nghĩa REST APIs cho Supabase
│       │   │   │   └── SupabaseConfig.kt       # Khai báo thông số BASE_URL và ANON_KEY
│       │   │   │
│       │   │   └── util/
│       │   │       ├── DoubleTapHandler.kt     # Handler xử lý phân biệt Chạm đơn (Single) & Chạm đúp (Double)
│       │   │       ├── LocationParser.kt       # Parser trích xuất tên địa điểm & Resolve short links
│       │   │       ├── NetworkUtils.kt         # Utility kiểm tra kết nối Internet (Active Network Capabilities)
│       │   │       ├── INetworkConnectivityObserver.kt # Interface theo dõi sự thay đổi trạng thái mạng
│       │   │       ├── NetworkConnectivityObserver.kt   # Implementation theo dõi mạng qua NetworkCallback
│       │   │       └── SupabaseConnectionGuard.kt       # Guard quản lý Cooldown & phòng ngừa spam request
│       │   │
│       │   └── res/
│       │       ├── layout/
│       │       │   ├── activity_main.xml       # Layout màn hình chọn chế độ
│       │       │   ├── activity_phone.xml      # Layout màn hình gửi địa điểm
│       │       │   ├── activity_car.xml        # Layout màn hình ô tô nhận vị trí
│       │       │   ├── activity_settings.xml   # Layout màn hình cài đặt (Chế độ Portrait)
│       │       │   ├── dialog_edit_favorite.xml# Layout dialog thêm/sửa địa điểm ưa thích
│       │       │   ├── dialog_favorites_card_popup.xml # Layout floating modal popup danh sách ưa thích (Car Mode)
│       │       │   ├── item_favorite.xml       # Layout phần tử danh sách địa điểm ưa thích (Settings)
│       │       │   └── item_favorite_card.xml  # Layout thẻ Card địa điểm ô tô cảm ứng lớn (Car Popup)
│       │       │
│       │       ├── layout-land/
│       │       │   └── activity_settings.xml   # Layout màn hình cài đặt tối ưu cho màn hình Ngang (Landscape)
│       │       │
│       │       ├── drawable/                   # Tài nguyên đồ họa XML (bg_dialog_card_popup, bg_card_favorite, bg_btn_open_map, scrollbar_thumb_car...)
│       │       └── values/                     # Colors, Strings, Themes (FloatingDialogTheme, Dark/Light Theme)
│       │
│       └── test/java/com/skul9x/locateshare/
│           ├── CarActivityAutoDismissWifiTest.kt      # Test tự động tắt Wi-Fi Dialog khi reconnect
│           ├── CarActivityFavoritesInteractionTest.kt # Test cử chỉ chạm đúp mở Popup Favorites
│           ├── CarActivityFloatingCardPopupTest.kt    # Test tích hợp Floating Card Popup & 85% Blur trong CarActivity
│           ├── CarActivitySupabaseConnectionFailureTest.kt # Test xử lý khi Supabase lỗi kết nối
│           ├── ExampleUnitTest.kt                     # Sample test
│           │
│           ├── adapter/
│           │   └── FavoriteCardAdapterTest.kt         # Test RecyclerView Adapter thẻ ô tô cho Floating Popup
│           │
│           ├── layout/
│           │   ├── FloatingCardPopupXmlTest.kt        # Test XML layout dialog popup thẻ & item card ô tô
│           │   ├── SettingsLandscapeLayoutTest.kt     # Test XML layout cài đặt ở chế độ ngang
│           │   ├── SettingsLayoutStructureTest.kt      # Test cấu trúc View của Settings
│           │   ├── SettingsPortraitLayoutTest.kt       # Test XML layout cài đặt ở chế độ dọc
│           │   └── SettingsStatusbarInsetsTest.kt     # Test bù trừ thanh trạng thái (StatusBar Insets)
│           │
│           ├── network/
│           │   └── SupabaseIntegrationTest.kt         # Test kết nối trực tiếp đến Supabase API
│           │
│           └── util/
│               ├── CarLayoutXmlTest.kt                # Test kiểm tra thuộc tính XML của activity_car.xml
│               ├── DoubleTapHandlerTest.kt            # Unit test thuật toán phân tách Single/Double tap
│               ├── LocationParserTest.kt              # Unit test bộ tách tên địa điểm & Redirect resolver
│               ├── ManifestNetworkPermissionTest.kt   # Test kiểm tra khai báo quyền mạng trong Manifest
│               ├── NetworkConnectivityObserverTest.kt # Test observer lắng nghe thay đổi mạng
│               ├── NetworkUtilsTest.kt                # Test các hàm helper kiểm tra mạng
│               └── WifiSettingsIntentTest.kt          # Test khởi tạo Intent mở Cài đặt Wi-Fi
```

---

## 🧩 2. Mô tả Chi tiết Các Class Cốt Lõi (Core Components Catalog)

### A. Tầng Giao diện (Activities & UI Layer)
1. **`MainActivity.kt`**:
   - Màn hình trung tâm giúp người dùng lựa chọn chuyển qua **Chế độ Xe Hơi (`CarActivity`)** hoặc **Chế độ Điện Thoại (`PhoneActivity`)**.
   - Lưu trữ lựa chọn mặc định vào `SharedPreferences` để tự động mở đúng màn hình ở các lần khởi chạy sau.
2. **`PhoneActivity.kt`**:
   - Nhận dữ liệu được chia sẻ từ Google Maps qua `Intent.ACTION_SEND` (Text/Plain).
   - Sử dụng `LocationParser` để làm sạch tên địa điểm và giải mã link rút gọn (`maps.app.goo.gl`) thông qua các `HEAD` request bất đồng bộ.
   - Gửi payload `{url, name}` lên bảng `current_location` (ID = 1) trên Supabase REST API.
3. **`CarActivity.kt`**:
   - Màn hình chính chạy trên màn hình xe Android / Điện thoại phụ.
   - Tích hợp `DoubleTapHandler` trên nút `btnFavorites`:
     - **Chạm đơn (Single Tap):** Gọi `openStarredFavorite()` mở địa điểm mặc định (⭐).
     - **Chạm đúp (Double Tap):** Gọi `showFavoritesPopup()` mở Floating Card Modal Popup danh sách ưa thích.
   - **Cơ chế 85% Background Blur & Dimming (`showFavoritesPopup`):**
     - Áp dụng `window.setDimAmount(0.85f)` làm tối nền 85% trên mọi phiên bản Android (API 24+).
     - Áp dụng `FLAG_BLUR_BEHIND` và `blurBehindRadius = 60` trên Android 12+ (API 31+) để tạo hiệu ứng mờ nhòe kính mờ phía sau modal.
     - Khung dialog bo góc nổi dạng thẻ card trên nền tối (`#1E1E1E`).
   - Tích hợp `NetworkConnectivityObserver`: Theo dõi mạng thời gian thực, tự động ẩn Dialog Wi-Fi khi có mạng trở lại và gọi `SupabaseConnectionGuard` để tự động tải dữ liệu mới.
4. **`SettingsActivity.kt`**:
   - Quản lý danh sách các địa điểm yêu thích (CRUD: Create, Read, Update, Delete).
   - Hỗ trợ đánh dấu vị trí mặc định ⭐ (`is_starred = true`).
   - Tự động thay đổi giao diện theo hướng màn hình (`layout` cho Portrait và `layout-land` cho Landscape).
   - Áp dụng WindowInsets API để bù khoảng trống cho thanh trạng thái hệ thống.
5. **`FavoriteAdapter.kt`**:
   - RecyclerView Adapter hiển thị danh sách các mục địa điểm ưa thích trong `SettingsActivity` với nút chỉnh sửa, xóa và nút đánh dấu sao.
6. **`FavoriteCardAdapter.kt`**:
   - RecyclerView Adapter tối ưu riêng cho xe hơi (Automotive Card UI) trong Floating Modal Popup của `CarActivity`.
   - Thiết kế thẻ Material Card nổi, chữ lớn (20sp+ bold), hiển thị huy hiệu ngôi sao ⭐ cho vị trí mặc định, nút hành động mở bản đồ **MỞ BẢN ĐỒ** kích thước 52dp+ dễ chạm khi đang lái xe.

### B. Tầng Mạng & Cloud (Network Layer)
1. **`ApiService.kt`**:
   - Định nghĩa các endpoint RESTful kết nối tới Supabase:
     - `getCurrentLocation()`: Truy vấn vị trí hiện tại (`GET /rest/v1/current_location?id=eq.1`).
     - `updateCurrentLocation()`: Cập nhật vị trí hiện tại (`PATCH /rest/v1/current_location?id=eq.1`).
     - `getFavoriteLocations()`: Lấy danh sách địa điểm ưa thích (`GET /rest/v1/favorite_locations`).
     - `addFavoriteLocation()` / `deleteFavoriteLocation()` / `updateFavoriteLocation()`: Quản lý danh sách ưa thích.
2. **`SupabaseConfig.kt`**:
   - Chứa hằng số cấu hình `BASE_URL` và `ANON_KEY`.

### C. Tầng Tiện ích & Xử lý Logic (Utilities & Logic Layer)
1. **`DoubleTapHandler.kt`**:
   - Xử lý cử chỉ chạm đúp độc lập với giao diện Android Views (có thể Unit Test 100% bằng JVM).
   - Sử dụng cơ chế Hẹn giờ (Scheduler/Timer): Nếu có lượt chạm thứ 2 trong khoảng thời gian `DEFAULT_DOUBLE_TAP_TIMEOUT_MS` (300ms), lượt chạm 1 sẽ bị hủy và sự kiện Double Tap được kích hoạt.
2. **`LocationParser.kt`**:
   - Phân tích và làm sạch nội dung chia sẻ từ Google Maps.
   - Theo vết chuyển hướng HTTP (Short link redirect resolution) bằng request `HEAD`.
3. **`NetworkConnectivityObserver.kt` & `INetworkConnectivityObserver.kt`**:
   - Đăng ký `ConnectivityManager.NetworkCallback` để nhận sự kiện mạng `ON_AVAILABLE`, `ON_LOST`, `ON_UNAVAILABLE` thời gian thực mà không cần dùng BroadcastReceiver cũ.
4. **`SupabaseConnectionGuard.kt`**:
   - Bảo vệ hệ thống khỏi việc gọi API dồn dập khi mạng chập chờn bật/tắt liên tục.
   - Áp dụng cơ chế Cooldown (mặc định 30 giây).

---

## 🔄 3. Cơ chế & Sơ đồ Tương tác Thành phần (Sequence & State Diagrams)

### A. Máy Trạng thái Xử lý Cử chỉ Chạm Đúp (`DoubleTapHandler`)

```
   [User Tap 1] --------> (State: Single Tap Pending)
                               |
            +------------------+------------------+
            |                                     |
    (300ms Timeout)                      [User Tap 2 (<300ms)]
            |                                     |
            v                                     v
[Execute: openStarredFavorite()]       [Cancel Timer & Execute: showFavoritesPopup()]
```

### B. Luồng Khởi tạo Floating Card Modal Popup & 85% Blur

```
[CarActivity: Double Tap on btnFavorites]
               │
               ▼
[Instantiate Custom Dialog (R.style.FloatingDialogTheme)]
               │
               ▼
[Configure Window Properties]
   ├── setBackgroundDrawable(TRANSPARENT)
   ├── setDimAmount(0.85f)  [85% Screen Dimming]
   └── API 31+: FLAG_BLUR_BEHIND (blurBehindRadius = 60)
               │
               ▼
[Fetch Favorites from Supabase DB]
               │
   ┌───────────┴───────────┐
   ▼                       ▼
(Favorites Found)      (Empty List)
   │                       │
   ▼                       ▼
[Bind FavoriteCardAdapter]  [Show Empty State Hint]
   │                       │
   └───────────┬───────────┘
               │
               ▼
[User Taps Card / 'MỞ BẢN ĐỒ']
               │
               ▼
[openMap(url) & Dismiss Dialog]
```

### C. Luồng Khôi phục Mạng & Tự động Ẩn Dialog trong `CarActivity`

```
  Android OS Network Callback
               |
               v
  NetworkConnectivityObserver.onAvailable()
               |
               v
     CarActivity.onNetworkRestored()
               |
   +-----------+-----------+
   |                       |
   v                       v
[Dismiss Wi-Fi Dialog]   [SupabaseConnectionGuard.fetchLocationIfAllowed()]
                           |
                     (Passed 30s Cooldown?)
                           |
                +----------+----------+
                |                     |
              (Yes)                  (No)
                |                     |
                v                     v
      [Call Supabase API]     [Skip Request (Protect Bandwidth)]
```

---

## 🧪 4. Cấu trúc Bộ Kiểm thử (Test Suite Matrix)

Hệ thống kiểm thử bao gồm **86 Test Cases** được chia làm 4 nhóm chính:

| Nhóm Kiểm thử | Class Kiểm thử | Mục đích & Chi tiết |
| :--- | :--- | :--- |
| **Gesture & Logic** | `DoubleTapHandlerTest` | Kiểm tra tính chính xác của cử chỉ chạm đơn, chạm đúp, chạm quá 300ms, chạm 3 lần và hủy timer. |
| **Floating Popup & Automotive UI** | `CarActivityFloatingCardPopupTest` <br/> `FavoriteCardAdapterTest` <br/> `FloatingCardPopupXmlTest` | Kiểm tra hiển thị floating modal popup, thuộc tính mờ/tối 85%, adapter thẻ ô tô, binding dữ liệu, hành vi mở bản đồ và cấu trúc giao diện XML. |
| **Network & Guard** | `NetworkConnectivityObserverTest` <br/> `NetworkUtilsTest` <br/> `CarActivityAutoDismissWifiTest` <br/> `CarActivitySupabaseConnectionFailureTest` | Kiểm tra lắng nghe trạng thái mạng, tự động tắt popup Wi-Fi khi có lại mạng, và xử lý khi mất kết nối tới Supabase. |
| **Parser & Integration** | `LocationParserTest` <br/> `SupabaseIntegrationTest` | Kiểm tra giải mã tên địa điểm tiếng Việt có dấu, resolve link ngắn, và gọi API thực tế tới Supabase. |
| **Layout & System Insets** | `CarLayoutXmlTest` <br/> `SettingsPortraitLayoutTest` <br/> `SettingsLandscapeLayoutTest` <br/> `SettingsStatusbarInsetsTest` | Kiểm tra giao diện XML thích ứng đúng với hướng màn hình (Portrait/Landscape) và thanh trạng thái (StatusBar). |

---

## 📄 Bản quyền (License)

Copyright © 2026 Nguyễn Duy Trường (skul9x).
