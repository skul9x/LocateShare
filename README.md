# LocateShare

**LocateShare** là một dự án Android (Kotlin) đơn giản hỗ trợ chia sẻ liên kết Google Maps nhanh chóng từ điện thoại đang sử dụng gửi xuống màn hình Android trên ô tô (hoặc điện thoại khác dùng như màn hình phụ).

Được nâng cấp mạnh mẽ từ kiến trúc PHP sang **Supabase**, hệ thống giờ đây đã loại bỏ hoàn toàn các rào cản hosting miễn phí, cho phép đồng bộ realtime các vị trí và cả quản lý danh sách địa điểm yêu thích của riêng bạn!

## ✨ Tính năng nổi bật

- 📱 **Chế độ Điện Thoại (Gửi):** Mở app Google Maps, ấn nút Share và chọn LocateShare. Link sẽ auto gửi lên Cloud DB ngay lập tức.
- 🚗 **Chế độ Xe Hơi (Nhận):** Tự động fetch tọa độ trên Cloud. Bấm "MỞ BẢN ĐỒ" là nhảy ngay vào Google Maps để điều hướng.
- ⭐ **Danh sách Ưa Thích (Favorites):**
  - Quản lý kho điểm đến ưa thích ngay trong App (Cài đặt).
  - Không giới hạn số lượng mục yêu thích.
  - **Fav of Favs (⭐):** Đánh dấu sao cho địa điểm thường đi nhất. Nhấn nút "Ưa thích" ngay trên màn hình Xe Hơi để mở bản đồ ngay tới điểm đó trong 1 nốt nhạc!
  - Ấn giữ nút "Ưa thích" để mở màn hình popup chọn 1 trong các điểm đã lưu.
- ⚡ **Backend mạnh mẽ:** Data được sync qua Supabase REST API (đã thiết lập RLS Policy bảo mật với Anon Key). Không lo Bypass cookie hay PHP Hosting lỗi nữa.

## 🛠 Tech Stack

- **Ngôn ngữ:** Kotlin
- **Networking:** Retrofit2 + Gson + OkHttp 
- **Database:** Supabase (PostgreSQL - REST)
- **Architecture:** Standard Android Activities & Views

## 🗂 Cấu trúc DB Supabase 

**1. Bảng `current_location` (ID: 1 cố định):**
Lưu trữ tọa độ hiện tại được gửi từ điện thoại.

**2. Bảng `favorite_locations`:**
Lưu trữ danh sách các điểm ưa thích với trạng thái `is_starred` (chỉ 1 điểm được bật sao cùng lúc).

## 🚀 Tự động hóa Workflow (CI/CD)
Mọi lịch sử quá trình AI Build và Agent Data được đồng bộ trong thư mục `.brain`.

---
*Dự án dành cho cá nhân, open-source.*
