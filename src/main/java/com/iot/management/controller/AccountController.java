package com.iot.management.controller;
import com.iot.management.dto.*;
import com.iot.management.service.UserManagementService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;
@RestController @RequestMapping("/api/account")
public class AccountController {
 private final UserManagementService service;
 public AccountController(UserManagementService s) {service=s;}
 @GetMapping public UserResponse me() {return service.me();}
 @PutMapping public UserResponse update(@Valid @RequestBody ProfileRequest r) {return service.profile(r);}
 @PostMapping("/password") public Map<String,String> password(@Valid @RequestBody PasswordChangeRequest r) {service.password(r);return Map.of("message","Đã đổi mật khẩu. Vui lòng đăng nhập lại.");}
}
