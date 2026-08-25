# Hệ Thống Quản Lý Thiết Bị IoT (IoT Device Management)

Dự án phát triển hệ thống web quản lý các thiết bị IoT, hỗ trợ giao tiếp dữ liệu theo thời gian thực (Real-time) và bảo mật nâng cao.

## Thành viên nhóm & Phân công (Vertical Slicing)

1. **[Đinh Thiên Bảo]**: Module Xác thực, Phân quyền & Quản lý User (Spring Security, JWT, HttpOnly Cookie).
2. **[Ngô Quang Đạt]**: Module Quản lý thiết bị IoT (CRUD thiết bị, Upload ảnh lên Cloudinary).
3. **[Nguyễn Viết Đăng]**: Module Thu thập dữ liệu IoT & Giám sát Real-time (REST API cho Postman, WebSocket, Dashboard Chart).

---

## Tech Stack
- **Backend:** Java 17, Spring Boot 2.7.x, Spring MVC, Spring Data JPA, Spring Security, JWT, WebSocket.
- **Frontend:** HTML/CSS/JS, Thymeleaf, Bootstrap 5.
- **Database:** MySQL.
- **Storage:** Cloudinary.

### Giải thích Tech Stack
- **Spring MVC:** mô hình chia dự án thành 3 tầng (Model, View, Controller)
- **Spring Data JPA:** công cụ giao tiếp với Database
- **Spring Security:** Kiểm soát Authen và Author
- **JWT:** là một chuẩn JSON Web Token để xác thực và trao đổi thông tin an toàn giữa các bên.
- **WebSocket:** Giao thức giao tiếp hai chiều giữa client và server theo thời gian thực.
- **Thymeleaf:** nhúng dữ liệu từ backend lên HTML
- **Bootstrap:** CSS Framework
- **MySQL:** Relational Database
- **Cloudinary:** Lưu trữ và quản lý ảnh, trả về 1 image_url để MySQL lưu
---

## Yêu cầu môi trường (Prerequisites)

Để chạy được dự án này, các thành viên cần cài đặt các phần mềm sau:
1. **JDK 26**: Môi trường chạy Java.
2. **MySQL Server & MySQL Workbench** (hoặc DBeaver/Navicat): Để lưu trữ và xem dữ liệu.
3. **Eclipse (Enterprise Java and Web Developer)**.
4. **Git**: Để quản lý mã nguồn.
5. **Postman**: Để giả lập việc gửi dữ liệu từ thiết bị IoT.

---

## Hướng dẫn Cài đặt & Setup Môi trường

Trước mắt thì cấu hình các phần core để làm việc cơ bản trước qua video của thầy: https://www.youtube.com/watch?v=koL1_6OXjl0&list=PL0yFty_aTOFM_6P9Tu9AJN6UOJDpr-uFU

## Quy trình làm việc với Git (Dành cho Team)

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

## Quy ước làm việc với Git (Git Conventions)

### 1. Quy ước đặt tên Nhánh (Branch Naming)
**Cú pháp:** `<tên_thành_viên>/<tên_tính_năng>`

*Lưu ý: Viết chữ thường, không dấu và dùng dấu gạch ngang `-` để ngăn cách các từ.*

**Ví dụ:**
- `bao/jwt-auth-security`
- `dat/device-crud`
- `dang/websocket-dashboard`

---

### 2. Quy ước viết Commit Message
**Cú pháp:** `<action_thực_hiện>: <mô_tả_ngắn_gọn_bằng_tiếng_anh>`

Các `action_thực_hiện` (tiền tố) được quy định như sau:

- `feat`: Thêm một tính năng mới hoàn toàn (Feature).
- `fix`: Sửa một lỗi (Bug).
- `update`: Cập nhật hoặc chỉnh sửa một tính năng/code đã có sẵn.
- `ui`: Thêm hoặc thay đổi giao diện (HTML/CSS/Thymeleaf/Bootstrap).
- `docs`: Thay đổi tài liệu (Ví dụ: README, comment code).
- `refactor`: Tối ưu hóa, dọn dẹp lại code nhưng không làm thay đổi chức năng.

**Ví dụ:**
- `feat: Thêm chức năng phân quyền bằng Spring Security`
- `ui: Thiết kế xong form thêm mới thiết bị IoT`
- `update: Đổi logic lưu ảnh từ thư mục local sang Cloudinary`
- `fix: Sửa lỗi không nhận được Token khi đăng nhập`
- `docs: Cập nhật hướng dẫn cài đặt database vào README`

---

### 3. Workflow with Github
1. Kéo code mới nhất từ nhánh chính về máy:
   `git checkout main` -> `git pull origin main`
2. Tạo nhánh mới để bắt đầu làm việc:
   `git checkout -b bao/jwt-auth-security`
3. Code và commit theo cú pháp:
   `git add .` -> `git commit -m "feat: Khởi tạo module JWT"`
4. Đẩy nhánh lên GitHub:
   `git push origin thuan/login-jwt`
5. Lên GitHub tạo Pull Request (PR) để ghép code vào `main`.
