# Nguyễn Viết Đăng — IoT realtime và Dashboard

## Bản tích hợp
Nhánh local `dang/integration-complete` ghép nhánh Đăng (43fc45f), main/Bảo (cbcfbb2, 5a4a851) và Đạt (8112593). Bản gốc tại D:/WEB/iot-device-management được giữ nguyên. Chưa push GitHub.

## Chạy ứng dụng
Yêu cầu Java 17 trở lên, Maven và MySQL 8; tạo database `iot_management`. Mở PowerShell trong thư mục bản tích hợp:

```powershell
$env:DB_PASSWORD = 'mat-khau-MySQL-cua-ban'
$env:MAIL_USERNAME = 'email-gui-OTP'
$env:MAIL_PASSWORD = 'app-password-cua-ban'
# Nếu cần upload ảnh:
$env:CLOUDINARY_CLOUD_NAME = '...'
$env:CLOUDINARY_API_KEY = '...'
$env:CLOUDINARY_API_SECRET = '...'
mvn spring-boot:run
```
Các biến chuẩn `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` cũng có thể ghi đè cấu hình. Không ghi thông tin thật vào Git.

## Kịch bản bảo vệ
1. Đăng ký, xác thực OTP, đăng nhập; cấu hình email thật để thử OTP.
2. Tạo danh mục bằng ADMIN/MANAGER, tạo thiết bị bằng USER, ghi lại ID.
3. Mở `/dashboard` bằng tài khoản chủ thiết bị. Bật/tắt ở phần sửa thiết bị quyết định thống kê ACTIVE/INACTIVE; đây không phải trạng thái online tự suy ra từ cảm biến.
4. Import `docs/Dang_IoT_Dashboard.postman_collection.json`. Điền baseUrl, email, password, deviceId. Chạy đăng nhập và lấy CSRF trước khi gửi dữ liệu. Postman giữ cookie JWT/CSRF; thông tin đăng nhập không được lưu trong file export.
5. Gửi nhiệt độ 30°C: tăng bản ghi, cập nhật biểu đồ, không cảnh báo. Gửi 50°C: không cảnh báo. Gửi 60°C: hiện toast, lưu WARNING, tăng thống kê cảnh báo. Trong 10 giây chỉ tạo một cảnh báo cho cùng thiết bị; dữ liệu cảm biến vẫn lưu đầy đủ.
6. Đổi bộ lọc ID để xem 20 bản ghi gần nhất theo thứ tự cũ đến mới. Ngắt/kết nối lại để kiểm tra tự kết nối và tải lịch sử.
7. Tài khoản khác không được gửi/xem dữ liệu của chủ thiết bị; ADMIN xem toàn hệ thống. Thiết bị không tồn tại trả 404, số liệu sai trả 400.

## Mô phỏng liên tục
```powershell
$env:IOT_PASSWORD = 'mat-khau-tai-khoan'
python tools/simulate.py --email 'email-cua-ban' --devices 1 --interval 2 --hot-chance 0.3 --count 20
```
Mô phỏng tự đăng nhập và sử dụng cookie cùng CSRF. Thay ID bằng thiết bị thuộc tài khoản.

## Nội dung báo cáo phần Đăng
HTTP POST `/api/iot/data` đi qua JWT, CSRF và validation, kiểm tra thiết bị/quyền sở hữu, khóa bản ghi thiết bị khi nhận dữ liệu để tránh tạo cảnh báo trùng đồng thời. Service lưu `device_data`, kiểm tra ngưỡng cấu hình `iot.temperature-threshold` (mặc định 50), tra thời điểm cảnh báo gần nhất trong database và lưu `device_logs` nếu hết `iot.alert-cooldown-seconds` (10 giây). Transaction commit thành công mới phát điểm dữ liệu và cảnh báo.

WebSocket bắt tay tại `/ws` với cùng cookie JWT. SockJS cung cấp phương án dự phòng; STOMP định tuyến tin nhắn. Chủ thiết bị subscribe `/user/queue/data` và `/user/queue/alerts`; ADMIN subscribe `/topic/data` và `/topic/alerts`. Bộ lọc inbound từ chối topic toàn hệ thống cho người dùng thường, từ chối destination lạ và STOMP SEND. Chỉ HTTP API được nhận dữ liệu. Giữ kiểm tra origin cùng nguồn.

Chart.js hiển thị nhiệt độ/độ ẩm; client nhận dữ liệu mới, giới hạn 20 điểm và cập nhật thống kê, toast, bảng cảnh báo. Thời gian lưu dưới dạng Instant (UTC) và trình duyệt hiển thị theo múi giờ người xem. Lịch sử, thống kê và nhật ký đều giới hạn theo thiết bị thuộc tài khoản; ADMIN thấy tất cả. Khi chọn tất cả, biểu đồ là chuỗi mẫu xen kẽ của nhiều thiết bị; chọn ID khi phân tích từng cảm biến.

## Kiểm chứng và giới hạn
`mvn test` chạy H2 chế độ MySQL, kiểm tra module CRUD và IoT; `node --test src/test/js/devices.test.cjs` kiểm tra client CRUD. Bộ test không thay thế việc thử MySQL, SMTP, Cloudinary và trình duyệt thực tế. Thư viện giao diện tải từ CDN nên cần mạng khi demo. Simple broker chạy trong một tiến trình, không có hàng đợi bền vững; kết nối lại tải lịch sử 20 điểm gần nhất.

Trước khi triển khai thật, thay JWT secret, cấu hình HTTPS/cookie Secure và đổi các credential từng xuất hiện trong lịch sử Git. Bản tích hợp đã bỏ mật khẩu SMTP/database ghi trực tiếp trong cấu hình.
