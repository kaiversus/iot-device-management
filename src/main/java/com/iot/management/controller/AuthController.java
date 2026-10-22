package com.iot.management.controller;
import com.iot.management.dto.*;
import com.iot.management.service.AuthService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService service;
 @org.springframework.beans.factory.annotation.Value("${app.cookie-secure:false}") private boolean secure;
 public AuthController(AuthService service) {this.service=service;}
 @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r) {
  String jwt=service.authenticateUser(r);
  return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,ResponseCookie.from("jwt_token",jwt).httpOnly(true).secure(secure).sameSite("Lax").path("/").maxAge(86400).build().toString()).body(Map.of("message","Đăng nhập thành công"));
 }
 @PostMapping("/register") public Map<String,String> register(@Valid @RequestBody RegisterRequest r) {service.registerUser(r);return ok("Đã gửi OTP xác thực. Mã có hiệu lực 5 phút.");}
 @PostMapping("/resend-verification") public Map<String,String> resend(@Valid @RequestBody ForgotPasswordRequest r) {service.resendVerification(r.getEmail());return ok("Đã gửi lại OTP xác thực.");}
 @PostMapping("/forgot-password") public Map<String,String> forgot(@Valid @RequestBody ForgotPasswordRequest r) {service.processForgotPassword(r.getEmail());return ok("Đã gửi OTP đặt lại mật khẩu.");}
 @PostMapping("/reset-password") public Map<String,String> reset(@Valid @RequestBody ResetPasswordRequest r) {service.resetPassword(r);return ok("Đã đổi mật khẩu. Vui lòng đăng nhập lại.");}
 @PostMapping("/verify-account") public Map<String,String> verify(@Valid @RequestBody VerifyRequest r) {service.verifyAccount(r.email,r.otpCode);return ok("Đã xác thực tài khoản. Bạn có thể đăng nhập.");}
 private Map<String,String> ok(String message) {return Map.of("message",message);}
}
