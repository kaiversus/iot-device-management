# Hệ Thống Quản Lý Thiết Bị IoT (IoT Device Management)

Dự án phát triển hệ thống web quản lý các thiết bị IoT, hỗ trợ giao tiếp dữ liệu theo thời gian thực (Real-time) và bảo mật nâng cao.

## 👥 Thành viên nhóm & Phân công (Vertical Slicing)

1. **[Đinh Thiên Bảo]**: Module Xác thực, Phân quyền & Quản lý User (Spring Security, JWT, HttpOnly Cookie).
2. **[Ngô Quang Đạt]**: Module Quản lý thiết bị IoT (CRUD thiết bị, Upload ảnh lên Cloudinary).
3. **[Nguyễn Viết Đăng]**: Module Thu thập dữ liệu IoT & Giám sát Real-time (REST API cho Postman, WebSocket, Dashboard Chart).

---

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot 2.7.x, Spring MVC, Spring Data JPA, Spring Security, JWT, WebSocket.
- **Frontend:** HTML/CSS/JS, Thymeleaf, Bootstrap 5.
- **Database:** MySQL.
- **Storage:** Cloudinary.

---

## 💻 Yêu cầu môi trường (Prerequisites)

Để chạy được dự án này, các thành viên cần cài đặt các phần mềm sau:
1. **JDK 26**: Môi trường chạy Java.
2. **MySQL Server & MySQL Workbench** (hoặc DBeaver/Navicat): Để lưu trữ và xem dữ liệu.
3. **Eclipse (Enterprise Java and Web Developer)**.
4. **Git**: Để quản lý mã nguồn.
5. **Postman**: Để giả lập việc gửi dữ liệu từ thiết bị IoT.

---

## 🚀 Hướng dẫn Cài đặt & Setup Môi trường (Dành cho thành viên)

Trước mắt thì cấu hình các phần core để làm việc cơ bản trước qua video của thầy: https://www.youtube.com/watch?v=koL1_6OXjl0&list=PL0yFty_aTOFM_6P9Tu9AJN6UOJDpr-uFU

## 🌳 Quy trình làm việc với Git (Dành cho Team)

**Tuyệt đối KHÔNG code và đẩy thẳng lên nhánh `main`.**

1. Trước khi code, luôn kéo code mới nhất về:
   ```bash
   git checkout main
   git pull origin main
   ```
2. Tạo nhánh mới theo tính năng bạn được phân công:
   ```bash
   git checkout -b feature/ten-tinh-nang # Ex: git checkout -b feature/auth
   ```
3. Sau khi code và test xong:
   ```bash
   git add .
   git commit -m "Thêm chức năng đăng nhập JWT"
   git push origin feature/ten-tinh-nang
   ```
4. Lên GitHub, tạo **Pull Request** để nhóm review và Merge vào `main`.
