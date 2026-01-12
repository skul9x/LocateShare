# 📍 LocateShare

Ứng dụng Android giúp chia sẻ địa điểm từ điện thoại sang màn hình ô tô (hoặc thiết bị khác) một cách dễ dàng.

## ✨ Tính năng chính

- **Chia sẻ từ Google Maps:** Nhận link chia sẻ trực tiếp từ Google Maps (qua Intent).
- **Phone Mode (Gửi):** Gửi link địa điểm lên server.
- **Car Mode (Nhận):** Tự động nhận địa điểm mới nhất và mở bản đồ.
- **Hỗ trợ Hosting miễn phí:** Tích hợp cơ chế xác thực cookie để hoạt động tốt trên các hosting như `free.nf` (InfinityFree).

## 🚀 Cài đặt & Sử dụng

### 1. Backend Setup
- Upload file trong thư mục `backend/` lên hosting PHP.
- Tạo file `location.txt` (hoặc để script tự tạo) và set quyền ghi (777).

### 2. App Setup
- Cài đặt file APK lên cả điện thoại và màn hình xe.
- Mở app, nhập URL server (VD: `https://your-site.free.nf/`).
- Chọn chế độ mặc định:
    - **Điện thoại:** Chọn "Phone Mode".
    - **Xe:** Chọn "Car Mode".

### 3. Cách dùng
1. Trên điện thoại, tìm địa điểm trên Google Maps.
2. Chọn **Share** -> chọn **LocateShare**.
3. Màn hình xe sẽ tự động nhận diện và hiển thị địa điểm (hoặc bấm Reload).

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin
- **Networking:** Retrofit + OkHttp
- **Web Auth:** WebView (để bypass anti-bot của free hosting)
- **Architecture:** Mô hình đơn giản, xử lý trực tiếp trong Activity.

## 📝 Lưu ý

- App sử dụng cơ chế lưu cookie vào SharedPreferences để duy trì đăng nhập với hosting miễn phí, giúp trải nghiệm mượt mà hơn mà không cần xác thực lại liên tục.
