# Nghiệm thu bản hoàn thiện ngày 22/10/2026

Các phần chức năng còn thiếu so với README đã được bổ sung và kiểm thử tích hợp. Kết quả phản ánh mã nguồn hiện tại, không dùng để suy luận mức đóng góp cá nhân hay cam kết điểm chấm của giảng viên.

## Phạm vi từng thành viên

- Bảo: bổ sung hồ sơ, đổi mật khẩu, quản lý User theo khu vực, khóa tài khoản, thu hồi JWT khi đổi mật khẩu/quyền/đăng xuất; OTP mã hóa, hết hạn, giới hạn thử và gửi lại; CSRF cho thao tác ghi. Admin có cấu hình, nhà cung cấp, nhật ký. Báo cáo IoT mới đã có.
- Đạt: giữ CRUD, tìm kiếm/phân trang và kiểm tra ảnh; bổ sung quyền đọc theo khu vực, ẩn thao tác sửa/xóa khi không có quyền và chặn xóa thiết bị có lịch sử. Upload/thay/xóa ảnh Cloudinary đã kiểm tra thật.
- Đăng: giữ API nhận dữ liệu và dashboard; bổ sung dữ liệu/cảnh báo theo khu vực Manager, ngưỡng lưu DB, khóa ngoại lịch sử; sửa ghép lịch sử và realtime để không mất hoặc trùng điểm mới.

## Kết quả đã xác nhận

- Maven: 28 test, 0 lỗi, 0 thất bại; đóng gói JAR thành công. Log local: `target/final-tests.log`.
- JavaScript: 9 test thành công; log local: `target/final-js-tests.log`.
- MySQL thật: đăng nhập Admin; trang và API hồ sơ/người dùng/quản trị; CRUD danh mục/thiết bị/nhà cung cấp; nhận dữ liệu, thống kê, cảnh báo; xóa thiết bị có lịch sử trả 409. Fixture được dọn theo đúng ID tạo trong lần chạy.
- Gmail thật: đăng ký trả 200, người dùng nhận OTP, xác thực trả 200, đăng nhập trả 200 và đọc đúng hồ sơ.
- Cloudinary thật: upload ảnh, thay ảnh, xóa thiết bị thành công. Admin API trả 404 cho cả ảnh cũ và ảnh cuối sau dọn, xác nhận tài nguyên thử đã bị xóa.
- Trình duyệt thật: dashboard báo đã kết nối, nhận hai điểm 30°C và 65°C, tổng bản ghi tăng thành 2, cảnh báo tăng thành 1, biểu đồ và thông báo xuất hiện mà không tải lại trang. Ảnh bằng chứng: `images/dashboard-tested.png`.
- Cấu hình local chứa thông tin dịch vụ đã xác thực và MySQL root theo yêu cầu; file này cùng thư mục `.local` không được đưa vào Git.

## Thay đổi dữ liệu cần biết

Database sử dụng `iot_management`. Cột `users.otp_code` cũ đã tăng lên VARCHAR(100) để lưu BCrypt. Script nâng cấp nằm trong `migrations/001_otp_hash.sql`. Tài khoản email dùng thử đã được xác thực; thông tin đăng nhập thử nằm trong `.local/registration-test.json`. Tài khoản Admin khởi tạo và mật khẩu nằm trong cấu hình local.

## Giới hạn và phần cần nhóm chuẩn bị

- Các bài kiểm thử phân quyền/OTP lỗi dùng H2 và SMTP giả lập; luồng thành công được kiểm tra thêm với MySQL/Gmail thật. Không phải mọi tổ hợp lỗi dịch vụ ngoài đều đã được thử trực tiếp.
- Simple broker WebSocket dùng cho một tiến trình. JWT được kiểm tra khi xác thực HTTP/handshake; kết nối WebSocket đã mở không được chủ động đóng ngay khi quyền tài khoản thay đổi. Khi demo thay quyền, đăng xuất và kết nối lại.
- Chưa có thiết bị vật lý/heartbeat; “thiết bị đã có dữ liệu” không đồng nghĩa đang online. Nhà cung cấp là danh bạ độc lập, chưa liên kết vào thiết bị.
- Giao diện đã xem trực tiếp ở cửa sổ trình duyệt hiện có; chưa nghiệm thu trên toàn bộ trình duyệt và thiết bị di động.
- Điểm Git/nhánh/commit và khả năng bảo vệ phải do cả nhóm thực hiện. Không tạo commit hay push trong đợt hoàn thiện này. Không đánh dấu toàn bộ thang điểm là đạt chỉ dựa trên test.
- `KhaoSat_PhanTichHeThong.docx` cũ là mẫu thương mại điện tử; dùng `BaoCao_HeThong_IoT.docx` cho hệ thống hiện tại.
