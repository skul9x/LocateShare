# Phase 01: Layout Scrollability & Scrollbar Fix
Status: ✅ Completed

## Objective
Thêm container cuộn (`NestedScrollView`) và bật thanh cuộn (`android:scrollbars="vertical"`) cho màn hình Cài đặt (`SettingsActivity`).

## Implementation Steps
1. [x] Bọc nội dung trong `activity_settings.xml` bằng `androidx.core.widget.NestedScrollView`.
2. [x] Thêm thuộc tính `android:fillViewport="true"` và `android:scrollbars="vertical"`.
3. [x] Cấu hình `RecyclerView` (`@id/rvFavorites`): `isNestedScrollingEnabled = false` và `android:scrollbars="vertical"`.
4. [x] Tạo file layout riêng cho chế độ ngang `res/layout-land/activity_settings.xml` thiết kế 2 cột (Trái: Form nhập địa điểm, Phải: Danh sách ưa thích) tối ưu cho màn hình ô tô.

## Files to Create/Modify
- `app/src/main/res/layout/activity_settings.xml` - [MODIFY]
- `app/src/main/res/layout-land/activity_settings.xml` - [NEW]
- `app/src/main/java/com/skul9x/locateshare/SettingsActivity.kt` - [MODIFY]
