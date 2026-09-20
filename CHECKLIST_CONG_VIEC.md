# CHECKLIST CÔNG VIỆC TỪ A-Z (DỰ ÁN QUẢN LÝ THIẾT BỊ IOT)

Dưới đây là roadmap chi tiết từng bước (Step-by-step) dành cho 3 thành viên, từ lúc chưa có gì đến khi hoàn thành đem đi báo cáo.

---

## 📍 GIAI ĐOẠN 1: KHỞI TẠO & THIẾT KẾ (Cả nhóm làm chung - Tuần 1)
*Mục tiêu: Ra được khung dự án, cơ sở dữ liệu và code base ban đầu.*

- [ ] **Bước 1: Chốt Database (ERD)**
  - Cùng nhau thiết kế các bảng: `users`, `roles`, `user_roles`, `devices`, `categories`, `device_data` (lưu trữ chỉ số), `device_logs`.
- [ ] **Bước 2: Khởi tạo Project (Bảo)**
  - Lên Spring Initializr tạo project (Web, Spring Data JPA, MySQL Driver, Security, WebSocket, Thymeleaf, Lombok, Java Mail Sender).
- [ ] **Bước 3: Setup cấu trúc 3 tầng (Bảo)**
  - Tạo sẵn các package trống: `controller`, `service`, `repository`, `entity`, `dto`, `security`, `config`, `exception`.
  - Cấu hình file `application.properties` kết nối tới MySQL.
- [ ] **Bước 4: Đẩy code lên Github (Bảo)**
  - Tạo Repo trên Github -> Push code base nhánh `main`.
  - **Đạt & Đăng** clone code về máy chạy thử.
- [ ] **Bước 5: Chuẩn bị Template Giao diện (Đạt)**
  - Lên mạng tìm và tải 1 template HTML/Bootstrap admin (ví dụ: SB Admin 2, AdminLTE).
  - Tách header, sidebar, footer thành các fragment của Thymeleaf để tái sử dụng.

---

## 📍 GIAI ĐOẠN 2: CODE CHỨC NĂNG CỐT LÕI (Chia nhánh làm việc - Tuần 2, 3)
*Mục tiêu: Xong hệ thống Backend API và Logic riêng lẻ của từng người.*

### 🛡️ Đinh Thiên Bảo (Nhánh: `bao/jwt-auth-security`)
- [ ] Map các Entity `User`, `Role` vào Database bằng Annotations JPA.
- [ ] Cấu hình Spring Security cơ bản.
- [ ] Setup logic JWT (Tạo ra class JwtTokenProvider: Sinh token và Validate token).
- [ ] Viết API Đăng nhập và Đăng ký (Lưu mật khẩu mã hóa BCrypt).
- [ ] Viết logic Gửi Email chứa mã OTP khi Đăng ký / Quên mật khẩu.
- [ ] Cấu hình phân quyền: Chặn các API theo role (Vd: chỉ Admin mới vào được `/api/admin/**`).

### 📦 Ngô Quang Đạt (Nhánh: `dat/device-crud`)
- [ ] Map các Entity `Device`, `Category`.
- [ ] Viết DeviceRepository, CategoryRepository.
- [ ] Đăng ký API Key Cloudinary -> Viết class `CloudinaryService` dùng để upload ảnh.
- [ ] Viết các API cho Thiết bị (CRUD):
  - `POST`: Thêm thiết bị (kèm upload ảnh lên Cloudinary).
  - `GET`: Lấy danh sách thiết bị (có phân trang và tìm kiếm theo tên).
  - `PUT`: Sửa thiết bị.
  - `DELETE`: Xóa thiết bị.

### 📡 Nguyễn Viết Đăng (Nhánh: `dang/websocket-dashboard`)
- [ ] Map các Entity `DeviceData` (id, device_id, temperature, humidity, time).
- [ ] Viết Controller nhận dữ liệu (Giả lập IoT): `POST /api/iot/data` (Nhận json thông số từ Postman rồi lưu Database).
- [ ] Cấu hình WebSocket (Tạo class `@EnableWebSocketMessageBroker`).
- [ ] Viết logic: Nếu nhận `POST /api/iot/data` mà thông số (Nhiệt độ > 50) -> Kích hoạt lệnh bắn message qua luồng Websocket xuống client.

---

## 📍 GIAI ĐOẠN 3: GHÉP GIAO DIỆN (FRONTEND) VÀO API (Tuần 4)
*Mục tiêu: Mọi người ghép API của mình lên giao diện Thymeleaf (Có thể làm chung 1 nhánh `dev` hoặc làm trên nhánh riêng).*

- [ ] **Bảo:** 
  - Ghép giao diện trang Login, Register, Quên mật khẩu.
  - Sử dụng thẻ `sec:authorize="hasRole('ADMIN')"` trên file html để ẩn/hiện menu tương ứng với role.
- [ ] **Đạt:** 
  - Dùng vòng lặp `th:each` hiển thị danh sách thiết bị dạng bảng.
  - Xử lý form Thêm mới / Cập nhật thiết bị bằng HTML.
  - Code thanh phân trang (Pagination) và thanh tìm kiếm.
- [ ] **Đăng:** 
  - Tạo trang Dashboard thống kê (Vd: có bao nhiêu thiết bị, trạng thái bật/tắt).
  - Tích hợp thư viện Chart.js vẽ biểu đồ dữ liệu thiết bị.
  - Code file `app.js` phía Client (Dùng `SockJS`, `STOMP`) để lắng nghe Websocket -> Nếu có tin nhắn tới thì hiển thị thông báo Toast / Alert lên màn hình (Ví dụ: "Cảnh báo: Nhiệt độ thiết bị 1 đang quá cao!").

---

## 📍 GIAI ĐOẠN 4: TEST LỖI, MERGE CODE & BÁO CÁO (Tuần 5)
*Mục tiêu: Đảm bảo code chạy không lỗi, chuẩn bị kịch bản đi bảo vệ với GV.*

- [ ] **Bước 1: Merge Code:** Cả 3 tạo Pull Request gộp code vào `main`, giải quyết Conflict code (nếu có).
- [ ] **Bước 2: Xử lý Exception Toàn cục (Bảo):** Thêm các Validation (`@NotBlank`, `@Size`) và `@ControllerAdvice` để bắt lỗi nhập bậy bạ mà web không bị sập (trả về lỗi chữ đỏ cho người dùng).
- [ ] **Bước 3: Test toàn hệ thống (Cả nhóm):**
  - Đăng ký 1 user -> vào check mail lấy OTP -> đăng nhập.
  - User đó vào tự thêm 1 thiết bị, gắn ảnh.
  - Admin vào xem danh sách xem có thấy thiết bị của user đó không.
  - Bật Postman bắn Data vào thiết bị đó -> Xem biểu đồ có chạy không -> Xem màn hình có nảy Alert Websocket không.
- [ ] **Bước 4: Viết Báo Cáo File Word (Chia việc):**
  - **Bảo:** Viết kiến trúc, sơ đồ Security, luồng hoạt động của JWT và OTP.
  - **Đạt:** Viết sơ đồ Database, thiết kế giao diện, luồng xử lý ảnh Cloudinary.
  - **Đăng:** Viết luồng thu thập dữ liệu bằng Postman, giao thức Websocket, thiết kế Dashboard.
- [ ] **Bước 5: Tổng duyệt:** Quay 1 video demo ngắn phòng hờ rủi ro hôm bảo vệ mạng yếu. Chuẩn bị sẵn bộ Postman Collection để GV hỏi thì bật lên bắn request liền.
