# IoT Device Management System

A project to develop a web system for managing IoT devices, supporting real-time data communication and advanced security.

## Team Members & Assignments

1. **[Đinh Thiên Bảo]**: Authentication, Authorization & User Management Module (Spring Security, JWT, HttpOnly Cookie).
2. **[Ngô Quang Đạt]**: IoT Device Management Module (Device CRUD, Cloudinary Image Upload).
3. **[Nguyễn Viết Đăng]**: IoT Data Collection & Real-time Monitoring Module (REST API for Postman, WebSocket, Dashboard Chart).

---

## Tech Stack
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

## Prerequisites

To run this project, team members need to install the following software:
1. **JDK 26**: Java runtime environment.
2. **MySQL Server & MySQL Workbench** (or DBeaver/Navicat): To store and view data.
3. **Eclipse (Enterprise Java and Web Developer)**.
4. **Git**: For source code management.
5. **Postman**: To simulate sending data from IoT devices.

---

## Installation & Environment Setup Guide

Initially, configure the core parts for basic operation following the teacher's video tutorial: https://www.youtube.com/watch?v=koL1_6OXjl0&list=PL0yFty_aTOFM_6P9Tu9AJN6UOJDpr-uFU

## GitHub Workflow

**Absolutely DO NOT code and push directly to the `main` branch.**

1. Before coding, always pull the latest code:
   ```bash
   git checkout main
   git pull origin main
   ```
2. Create a new branch according to your assigned feature:
   ```bash
   git checkout -b feature/feature-name # Ex: git checkout -b feature/auth
   ```
3. After coding and testing is complete:
   ```bash
   git add .
   git commit -m "Add JWT login feature"
   git push origin feature/feature-name
   ```
4. Go to GitHub, create a **Pull Request** for the team to review and Merge into `main`.

## Git Conventions

### 1. Branch Naming
**Syntax:** `<member_name>/<feature_name>`

*Note: Use lowercase, no accents, and hyphens `-` to separate words.*

**Examples:**
- `bao/jwt-auth-security`
- `dat/device-crud`
- `dang/websocket-dashboard`

---

### 2. Commit Message Conventions
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

---

### 3. Workflow with GitHub
1. Pull the latest code from the main branch to your local machine:
   `git checkout main` -> `git pull origin main`
2. Create a new branch to start working:
   `git checkout -b bao/jwt-auth-security`
3. Code and commit using the syntax:
   `git add .` -> `git commit -m "feat: Initialize JWT module"`
4. Push the branch to GitHub:
   `git push origin bao/jwt-auth-security`
5. Go to GitHub and create a Pull Request (PR) to merge code into `main`.
