# Đánh giá bản tích hợp IoT — 21/10/2026

**Kết luận: chưa đủ căn cứ nghiệm thu toàn bộ yêu cầu ban đầu.** Đạt và Đăng cơ bản hoàn thành phần chức năng cốt lõi; Bảo đã có xác thực cơ bản nhưng còn thiếu User Management và có lỗi bảo mật/OTP cần sửa. Phần báo cáo và nghiệm thu chung chưa hoàn chỉnh.

Đánh giá theo sản phẩm trong repository, không suy luận năng lực hoặc công sức cá nhân từ số commit. Phân trách nhiệm theo README; những lỗi ở ranh giới module cần cả nhóm phối hợp.

## Căn cứ và phạm vi

- Bản `main`, commit `55f2959`; kiểm tra lịch sử Git, README đầu tiên và README hiện tại, CHECKLIST_CONG_VIEC.md, tài liệu module, nội dung văn bản trong Word, mã Java, HTML/CSS/JS, cấu hình, kiểm thử, Postman và script chạy/mô phỏng.
- Không coi file build trong target, dữ liệu nhị phân/cache IDE trong .metadata hoặc nội dung nội bộ .git là yêu cầu nghiệp vụ. Có 113 file được Git theo dõi, gồm cả metadata IDE.
- README đầu tiên đã giao Bảo phần User Management. README hiện tại mục 2 mô tả rõ User/Manager/Admin; đây là căn cứ đối chiếu chức năng. Không có bản đề chấm chính thức riêng trong repository để xác nhận thang điểm của giảng viên.
- Word `KhaoSat_PhanTichHeThong.docx` chứa khảo sát Tiki/Shopee/Lazada/Etsy, Customer/Vendor, đơn hàng, giỏ hàng, thanh toán. Có thể là mẫu tham khảo; không coi các tính năng thương mại điện tử là yêu cầu của ứng dụng IoT.
- Chưa chạy nghiệm thu trình duyệt, MySQL, SMTP và Cloudinary thật trên máy hiện tại. Tài liệu Đạt ghi nhận đã thử MySQL/Cloudinary ngày 15–16/10; đây là bằng chứng do tài liệu ghi lại, không phải lần kiểm chứng độc lập này.

## Kết quả kiểm thử trực tiếp

- `mvn test`: 17 test, 0 failures, 0 errors, 0 skipped. DeviceIntegrationTest 10; CloudinaryServiceTest 3; IotIntegrationTest 3; WebSocketAuthorizationTest 1.
- `node --test src/test/js/devices.test.cjs`: 8/8 đạt.
- Thêm tạm 4 phép thử chẩn đoán trên H2: cả 4 xác nhận hành vi lỗi mô tả bên dưới. Đây là test khẳng định lỗi tồn tại, không phải 4 tiêu chí đạt chất lượng.
- Log: `target/review-maven.log`, `target/review-audit.log`; mã phép thử lưu ở `target/ReviewAuditTest.java`. File test tạm đã được gỡ khỏi src/test sau khi chạy; không thay mã ứng dụng.
- Test hiện có chủ yếu kiểm tra thiết bị/IoT. MockMvc render HTML không tương đương chạy JavaScript trong trình duyệt; test WebSocket kiểm tra interceptor, chưa chứng minh kết nối thực từ trình duyệt tới broker.

## Từng thành viên

### Đinh Thiên Bảo — chưa đạt đầy đủ phạm vi giao

Đã có: cấu trúc project, User/Role, BCrypt, đăng nhập và JWT HttpOnly cookie, đăng ký/xác thực OTP, quên mật khẩu/đặt lại mật khẩu, giao diện auth, rule bảo vệ đường dẫn theo role, xử lý lỗi chung.

Còn thiếu theo README: xem/sửa thông tin cá nhân; quản lý/tìm kiếm User theo phạm vi Manager; quản trị tài khoản; nghiệp vụ cấp quyền/khóa tài khoản qua UI/API. Rule `/api/admin/**` và `/api/manager/**` có trong SecurityConfig nhưng không có controller nghiệp vụ tương ứng. Không có báo cáo riêng về Security/JWT/OTP hoàn chỉnh trong docs.

Luồng OTP và JWT có các lỗi R1–R4. Không nên đánh dấu “hệ thống bảo mật hoàn thiện”. Test hiện có không có bộ chuyên biệt cho đăng ký → email → xác thực → đăng nhập → quên mật khẩu.

### Ngô Quang Đạt — cơ bản đạt module Device CRUD/UI

Đã có: CRUD thiết bị/danh mục, tìm kiếm/lọc/phân trang, kiểm tra chủ sở hữu và Admin, validation, upload/đổi/xóa ảnh Cloudinary, kiểm tra nội dung file ảnh, dọn ảnh theo kết quả transaction, Thymeleaf/Bootstrap và tài liệu/Postman/test khá đầy đủ.

Cần phối hợp sửa chính sách xóa thiết bị đã có dữ liệu (R5). Manager hiện chỉ quản lý thiết bị của mình; tài liệu Đạt đã nói rõ giới hạn schema, vì vậy thiếu thống kê theo khu vực là thiếu sót tích hợp, không quy toàn bộ cho riêng Đạt. Dashboard chưa dùng layout chung; cần thống nhất giao diện với Đăng. Chưa chấm chắc tiêu chí “đẹp, responsive” nếu chưa xem trên trình duyệt và điện thoại.

### Nguyễn Viết Đăng — cơ bản đạt module IoT/realtime/dashboard

Đã có: nhận và lưu dữ liệu, validation, kiểm tra thiết bị/quyền sở hữu, cảnh báo nhiệt độ >50°C, cooldown, gửi sau commit, WebSocket riêng chủ sở hữu và topic Admin, biểu đồ/lịch sử/thống kê/log, tự kết nối lại, mô phỏng và Postman, tài liệu và test.

Cần phối hợp sửa quan hệ lịch sử–thiết bị (R5), bổ sung thống kê theo phạm vi Manager khi schema được hoàn thiện. Chỉ số “Thiết bị đang gửi dữ liệu” đang đếm thiết bị từng có dữ liệu, không có khoảng thời gian gần đây. Tài liệu còn ghi “chưa push” dù lịch sử main đã có PR #3. Collection cũ trong tools thiếu đăng nhập/CSRF; nên bỏ hoặc ghi rõ dùng collection mới trong docs.

## Phát hiện ưu tiên

### R1 — P1: Khóa ký JWT nằm cố định trong source

Vị trí: `src/main/resources/application.properties:17`, `security/JwtTokenProvider.java`.

Ứng dụng ký/kiểm tra token bằng khóa được commit. Người có source có thể tạo token mang email của một tài khoản đã tồn tại; filter tải quyền của tài khoản đó từ DB. Nếu giữ nguyên khóa này khi triển khai, bảo vệ JWT mất ý nghĩa đối với người biết khóa. Chuyển sang cấu hình bí mật của môi trường và thay khóa hiện tại. Không chỉ xóa dòng khỏi bản mới rồi tiếp tục dùng cùng khóa.

### R2 — P1: JWT bỏ qua trạng thái enabled

Vị trí: `security/JwtAuthenticationFilter.java:40`.

Filter tải UserDetails rồi tạo Authentication mà không kiểm tra `isEnabled()`. Phép thử: tạo JWT → đặt enabled=false → GET /api/devices bằng JWT cũ vẫn trả 200. Cần từ chối tài khoản vô hiệu hóa. Việc reset mật khẩu cũng chưa có cơ chế thu hồi token cũ; token cũ còn hợp lệ tới hạn nếu khóa không đổi.

### R3 — P1: OTP không có thời hạn, số lần thử hoặc mục đích

Vị trí: `entity/User.java`, `service/AuthService.java:99`, `service/MailService.java`.

User chỉ lưu otpCode; không có expiresAt, attempt counter hoặc purpose. Verify/reset chỉ so chuỗi. Phép thử xác nhận mã đặt cho tài khoản chưa xác thực được nhận tại reset-password và kích hoạt tài khoản. Việc dùng chung mã không tự chứng minh chiếm tài khoản nếu không biết OTP, nhưng chứng minh hai luồng không được tách. Mã vẫn dùng được cho đến khi bị thay/xóa; không có giới hạn thử ở tầng ứng dụng. Cần OTP có thời hạn, giới hạn thử/gửi lại, gắn mục đích; dùng bộ sinh ngẫu nhiên phù hợp cho mã bảo mật.

### R4 — P1: Email lỗi vẫn báo thành công

Vị trí: `service/AuthService.java:78`, `service/AuthService.java:93`.

sendOtpEmail ném lỗi bị bắt và chỉ in log. Controller vẫn trả 200 với thông báo đã gửi. Phép thử mock SMTP lỗi xác nhận forgot-password vẫn trả “OTP sent to your email!”. Đăng ký còn lưu tài khoản disabled trước khi gửi; người dùng thử đăng ký lại bị trùng email và UI không có luồng gửi lại xác thực riêng. Cần trạng thái gửi rõ ràng, xử lý thất bại và cơ chế gửi lại.

### R5 — P2: Xóa thiết bị để lại dữ liệu lịch sử mồ côi

Vị trí: `service/DeviceService.java:76`, `entity/DeviceData.java:15`, `entity/DeviceLog.java:15`.

Hai entity chỉ lưu Long deviceId, không khai báo khóa ngoại tới Device. delete chỉ xóa Device. Phép thử trên DB sạch: có 1 thiết bị + 1 mẫu đo + 1 cảnh báo; xóa thiết bị trả 204, hai bản ghi con còn trong DB, API lịch sử trả [] vì lọc qua danh sách thiết bị hiện tồn tại. Cần thống nhất soft delete giữ lịch sử, chặn xóa, hoặc xóa liên quan trong transaction và ràng buộc DB. Nếu DB cũ có FK tự tạo ngoài source, hành vi có thể khác; cần kiểm tra MySQL triển khai.

### R6 — Thiếu yêu cầu nghiệp vụ User/Manager/Admin

Căn cứ: `README.md:24–26`, `docs/DAT_DEVICE_MODULE.md:109`.

Chưa có profile/User Management, quan hệ Manager–User/khu vực, thống kê khu vực, quản lý nhà cung cấp/cấu hình hệ thống và system audit logs. Danh mục thiết bị đã có; cảnh báo nhiệt độ trong device_logs không thay thế audit log tài khoản/thao tác hệ thống. Bảo chủ trì User Management/role; Đạt và Đăng nối phạm vi thiết bị và thống kê sau khi nhóm chốt schema.

### R7 — P2: Dashboard có thể ghi đè điểm realtime mới khi tải lịch sử

Vị trí: `static/js/app.js`, loadHistory và addPoint.

loadHistory gửi HTTP rồi khi hoàn tất thay toàn bộ mảng biểu đồ. Trong lúc chờ, addPoint có thể thêm một điểm mới từ WebSocket; phản hồi lịch sử chụp trước điểm đó sẽ ghi đè và làm điểm biến mất. historyVersion chỉ loại phản hồi của lần tải lịch sử cũ, không bảo vệ cập nhật WebSocket. Đây là đường đua xác định qua đọc mã, chưa tái hiện trong trình duyệt. Cần đệm/ghép dữ liệu theo ID hoặc trình tự đồng bộ và kiểm thử reconnect khi có dữ liệu liên tục.

### R8 — Báo cáo và bàn giao chưa khớp sản phẩm

Word hiện là nội dung thương mại điện tử, có thể dùng làm mẫu nhưng chưa phải báo cáo IoT. Có hai báo cáo Markdown của Đạt/Đăng, chưa thấy bản Word IoT hoàn chỉnh, báo cáo bảo mật của Bảo hoặc video demo. Checklist còn mục trống cho các tính năng thực tế đã làm; tài liệu Đăng ghi chưa push trong khi main có merge PR #3. Không dùng checkbox cũ để kết luận ai chưa làm.

## Đối chiếu tiêu chí tự chấm trong README

- Quản lý/Git: có phân công và merge PR #1, #2, #3. Không kết luận tuân thủ tuyệt đối mọi lần push từ lịch sử local. Đang track .metadata IDE; nên dọn khỏi Git và ignore.
- Kiến trúc 3 tầng: đã có Controller/Service/Repository, DTO và entity.
- Logic/validation/security 4 role (mục có trọng số 6 điểm): chưa đầy đủ vì R1–R6. Đây là phần có rủi ro mất điểm lớn nhất theo checklist nội bộ.
- UI: đã triển khai Bootstrap/Thymeleaf, nhưng thiếu nghiệm thu trực quan và chưa đồng nhất dashboard/auth/ngôn ngữ.
- Sáng tạo: đã hiện thực JWT, Cloudinary và realtime; cần chứng minh luồng thật chạy liên tục.
- Báo cáo/phản biện: tài liệu module có ích nhưng Word chưa khớp IoT; không thể đánh giá khả năng trình bày của từng người từ code.

Không gán điểm số hoặc phần trăm hoàn thành tùy ý; chưa có rubric chi tiết và nghiệm thu thực tế để tính đáng tin cậy.

## Thứ tự hoàn thiện

1. Bảo sửa khóa JWT, enabled/token lifecycle và OTP/email; viết test auth gồm lỗi SMTP, hết hạn OTP, quá số lần thử, sai mục đích và tài khoản disabled.
2. Nhóm đối chiếu đề chính thức để hoàn thiện đúng phạm vi User/Manager/Admin trong README; nếu đã có thay đổi phạm vi được chấp thuận thì cập nhật tài liệu và ghi nhận rõ.
3. Đạt + Đăng thống nhất xóa thiết bị/lưu lịch sử và ràng buộc DB; test xuyên module.
4. Đăng sửa cách đồng bộ history/realtime và nhãn thống kê; phối hợp Đạt thống nhất layout; cập nhật Postman/tài liệu.
5. Cả nhóm chạy nghiệm thu MySQL + email OTP thật + Cloudinary thật + hai tài khoản trình duyệt + Admin/Manager; kiểm tra vượt ngưỡng, phân quyền, reconnect, responsive. Hoàn thiện Word IoT và video dự phòng.
