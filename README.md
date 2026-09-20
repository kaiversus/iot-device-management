# IoT Device Management System

A project to develop a web system for managing IoT devices, supporting real-time data communication and advanced security.

## 1. Team Members & Assignments (Nhóm 3 người)

1. **[Đinh Thiên Bảo]**: Đóng vai trò module Authentication, Authorization & User Management (Spring Security, JWT, HttpOnly Cookie).
   - **Task:** Thiết kế Database, cấu hình cơ bản Spring Boot. Cấu hình Spring Security, tích hợp JWT, chức năng Login, Register, Quên mật khẩu (có gửi mã OTP qua Email). Thiết lập phân quyền (chặn các endpoint theo role Guest, User, Manager, Admin).
   - **Mục tiêu & Báo cáo:** Hoàn thành base logic chuẩn 3 tầng, hệ thống bảo mật hoàn thiện. Đảm nhiệm viết báo cáo về bảo mật, cấu trúc JWT, cấu trúc User Management.

2. **[Ngô Quang Đạt]**: Đóng vai trò module IoT Device Management (Device CRUD, Cloudinary Image Upload) & Frontend UI.
   - **Task:** Xây dựng CRUD API quản lý thiết bị IoT, chức năng phân trang, lọc/tìm kiếm thiết bị. Xử lý tích hợp Cloudinary để upload hình ảnh thiết bị. Thiết kế và code giao diện Thymeleaf + Bootstrap cho hệ thống.
   - **Mục tiêu & Báo cáo:** Đạt 1 điểm Giao diện (đẹp, Responsive, Bootstrap), logic quản lý thiết bị mượt mà. Đảm nhiệm viết báo cáo phần Device CRUD và thiết kế UI/UX.

3. **[Nguyễn Viết Đăng]**: Đóng vai trò module IoT Data Collection & Real-time Monitoring (REST API for Postman, WebSocket, Dashboard Chart).
   - **Task:** Viết API nhận dữ liệu từ các thiết bị (sử dụng Postman để giả lập data gửi về). Cấu hình WebSocket để gửi thông báo/cảnh báo realtime về giao diện nếu thông số vượt ngưỡng. Vẽ biểu đồ Dashboard thống kê thiết bị và dữ liệu (Chart.js/Thymeleaf).
   - **Mục tiêu & Báo cáo:** Chức năng sáng tạo (Websocket, Data collection) hoạt động tốt. Đảm nhiệm viết báo cáo phần Real-time, logic luồng thông báo và thống kê.

---

## 2. Các Vai Trò (Roles) & Chức Năng Cốt Lõi 
Hệ thống sử dụng 4 roles cơ bản để phục vụ nghiệp vụ Quản lý IoT:
1. **Guest:** Xem trang chủ (hiển thị thông tin chung), đăng ký tài khoản (gửi OTP email), quên mật khẩu (nhận OTP email).
2. **User:** Đăng nhập/Đăng xuất, quản lý thông tin cá nhân, quản lý danh sách thiết bị IoT của mình, xem biểu đồ lịch sử hoạt động, nhận cảnh báo realtime qua Websocket.
3. **Manager:** Có quyền của User, cộng thêm quản lý/tìm kiếm Users trong phạm vi quản lý, thống kê thiết bị chung của khu vực, quản lý danh mục.
4. **Admin:** Quản trị toàn hệ thống, cấu hình hệ thống (nhà cung cấp, loại thiết bị), theo dõi tài khoản và system logs.

---

## 3. Tech Stack
- **Backend:** Java 17, Spring Boot 2.7.x, Spring MVC, Spring Data JPA, Spring Security, JWT, WebSocket.
- **Frontend:** HTML/CSS/JS, Thymeleaf, Bootstrap 5.
- **Database:** MySQL.
- **Storage:** Cloudinary.

### Tech Stack Explanation
- **Spring MVC:** An architectural pattern that separates the project into 3 layers (Model, View, Controller).
- **Spring Data JPA:** A tool for communicating with the Database.
- **Spring Security:** Controls Authentication and Authorization.
- **JWT:** JSON Web Token standard for secure authentication and information exchange between parties.
- **WebSocket:** A two-way, real-time communication protocol between client and server.
- **Thymeleaf:** Embeds data from the backend into HTML.
- **Bootstrap:** CSS Framework.
- **MySQL:** Relational Database.
- **Cloudinary:** Stores and manages images, returning an image_url for MySQL to save.

---

## 4. Prerequisites

To run this project, team members need to install the following software:
1. **JDK 26** (hoặc tương thích bản Java của nhóm): Java runtime environment.
2. **MySQL Server & MySQL Workbench** (or DBeaver/Navicat): To store and view data.
3. **Eclipse (Enterprise Java and Web Developer)** / Spring Tool Suite (STS).
4. **Git**: For source code management.
5. **Postman**: To simulate sending data from IoT devices.

---

## 5. Installation & Environment Setup Guide

Initially, configure the core parts for basic operation following the teacher's video tutorial: [Youtube Video Link](https://www.youtube.com/watch?v=koL1_6OXjl0&list=PL0yFty_aTOFM_6P9Tu9AJN6UOJDpr-uFU)

---

## 6. GitHub Workflow & Git Conventions

**Absolutely DO NOT code and push directly to the `main` branch.**

### Branch Naming
**Syntax:** `<member_name>/<feature_name>`
*Note: Use lowercase, no accents, and hyphens `-` to separate words.*
**Examples:**
- `bao/jwt-auth-security`
- `dat/device-crud`
- `dang/websocket-dashboard`

### Commit Message Conventions
**Syntax:** `<action_type>: <short_description_in_english>`
The `action_type` (prefix) is defined as follows:
- `feat`: Add a completely new feature.
- `fix`: Fix a bug.
- `update`: Update or modify an existing feature/code.
- `ui`: Add or change the UI (HTML/CSS/Thymeleaf/Bootstrap).
- `docs`: Change documentation (e.g., README, code comments).
- `refactor`: Optimize and clean up code without changing functionality.

**Examples:**
- `feat: Add role-based authorization with Spring Security`
- `ui: Design the new IoT device form`
- `update: Change image saving logic from local to Cloudinary`
- `fix: Resolve issue with missing Token on login`
- `docs: Update database setup guide in README`

### Workflow with GitHub
1. Pull the latest code from the main branch to your local machine:
   ```bash
   git checkout main
   git pull origin main
   ```
2. Create a new branch to start working:
   ```bash
   git checkout -b bao/jwt-auth-security
   ```
3. Code and commit using the syntax:
   ```bash
   git add .
   git commit -m "feat: Initialize JWT module"
   ```
4. Push the branch to GitHub:
   ```bash
   git push origin bao/jwt-auth-security
   ```
5. Go to GitHub, create a **Pull Request (PR)** for the team to review and Merge into `main`.

---

## 7. Tiêu Chí Chấm Điểm (Thang 10 - Self-Checklist)
- [ ] **1 điểm:** Quản lý, phân công nhiệm vụ và có quá trình commit Github đúng chuẩn nhánh/tên commit.
- [ ] **0.5 điểm:** Đúng cấu trúc project theo mô hình 3 tầng (Controller/Service/Repository).
- [ ] **6 điểm:** Đúng và đầy đủ tính logic, validation, security của từng role (Guest/User/Manager/Admin).
- [ ] **1 điểm:** Giao diện đẹp, dễ sử dụng và Responsive (Bootstrap).
- [ ] **1 điểm:** Chức năng sáng tạo (Websocket realtime notification, JWT, Upload ảnh Cloudinary).
- [ ] **0.5 điểm:** Khả năng báo cáo và trả lời phản biện (Mỗi thành viên report theo module/role của mình).
