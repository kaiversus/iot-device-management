package com.iot.management.controller;

import com.iot.management.dto.*;
import com.iot.management.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.registerUser(registerRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully! Please check your email for OTP.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.processForgotPassword(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP sent to your email!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestBody Map<String, String> request) {
        try {
            authService.verifyAccount(request.get("email"), request.get("otpCode"));
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account verified successfully! You can now login.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
