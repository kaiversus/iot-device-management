# Module IoT Realtime & Dashboard (Nguyễn Viết Đăng)

Nhánh: `dang/websocket-dashboard`

## 1. Luồng hoạt động
```
Postman / tools/simulate.py
   │ POST /api/iot/data {deviceId, temperature, humidity}
   ▼
IotController → DeviceDataService.save()
   ├─ lưu bảng device_data
   ├─ convertAndSend("/topic/data")    → điểm mới cho biểu đồ
   └─ nếu temperature > iot.temperature-threshold (và hết cooldown)
        ├─ lưu bảng device_logs (WARNING)
        └─ convertAndSend("/topic/alerts") → Toast + bảng cảnh báo
   ▼ (WebSocket / STOMP)
templates/dashboard.html + static/js/app.js (SockJS + STOMP + Chart.js)
```

## 2. Kiến thức cần nắm
| Khái niệm | Ý nghĩa ngắn gọn |
|---|---|
| WebSocket | Kết nối 2 chiều, server chủ động đẩy dữ liệu (HTTP chỉ hỏi-đáp). |
| STOMP | Giao thức nhắn tin chạy trên WebSocket: có destination, subscribe, send. |
| SockJS | Lớp dự phòng khi trình duyệt/mạng không hỗ trợ WebSocket thuần. |
| `/topic/...` | Kênh broadcast: server gửi, mọi client đã subscribe đều nhận. |
| `/app/...` | Prefix cho message client gửi lên server (`@MessageMapping`); dự án hiện chưa dùng. |
| SimpMessagingTemplate | Bean để bất kỳ Service nào cũng đẩy message xuống client. |
| Chart.js | Vẽ line chart, cập nhật bằng `chart.update()` không cần reload trang. |

## 3. Cách chạy & test
1. Tạo DB `iot_management` trong MySQL (Hibernate tự tạo bảng `device_data`, `device_logs`).
2. Chạy app, đăng ký/đăng nhập, mở `http://localhost:8080/dashboard`.
3. Import `tools/IoT_Dang.postman_collection.json` vào Postman:
   - Request 1: chỉ biểu đồ nhảy thêm điểm.
   - Request 2: biểu đồ nhảy + Toast đỏ + dòng mới ở bảng cảnh báo.
   - Request 3, 4: trả về 400 kèm thông báo lỗi validate.
4. Demo tự động: `python tools/simulate.py --devices 1 2 --interval 2 --hot-chance 0.2`

Ngưỡng và thời gian chờ cảnh báo chỉnh trong `application.properties`:
`iot.temperature-threshold`, `iot.alert-cooldown-seconds`.

## 4. Việc cần phối hợp
- **Bảo:** đã thêm 2 dòng vào `SecurityConfig`: `POST /api/iot/data` và `/ws/**` là `permitAll`. Các `GET /api/iot/**` và `/dashboard` yêu cầu đăng nhập (cookie `jwt_token`). Nhờ Bảo review khi merge.
- **Đạt:** hiện chưa có entity `Device`, nên `DeviceData` đang lưu `deviceId` dạng số. Sau khi Đạt merge:
  1. Trong `DeviceData` đổi thành:
     ```java
     @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false)
     private Device device;
     ```
  2. Trong `DeviceDataService.save()` dùng `deviceRepository.findById(...)` để báo lỗi nếu không tồn tại và lấy `device.getName()` cho câu cảnh báo.
  3. Thẻ "Thiết bị đang gửi dữ liệu" trên dashboard đổi sang đếm từ bảng `devices` (tổng, bật/tắt) khi Device có trường trạng thái.

## 5. Việc còn lại của Đăng
- [ ] Đợi/hỏi Đạt chốt entity `Device` (id, name, status) rồi làm bước 4 ở trên.
- [ ] Tích hợp dashboard vào layout chung (navbar/sidebar) của Đạt.
- [ ] Lọc dữ liệu theo user: User chỉ thấy thiết bị của mình, Manager/Admin thấy tất cả (cần Device có owner).
- [ ] Viết báo cáo: luồng Postman → API → DB → WebSocket, giao thức STOMP, thiết kế Dashboard.
- [ ] Quay video demo dự phòng.
