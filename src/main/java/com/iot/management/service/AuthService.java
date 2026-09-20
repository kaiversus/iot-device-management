package com.iot.management.service;

import com.iot.management.dto.LoginRequest;
import com.iot.management.dto.RegisterRequest;
import com.iot.management.dto.ResetPasswordRequest;
import com.iot.management.entity.Role;
import com.iot.management.entity.User;
import com.iot.management.repository.RoleRepository;
import com.iot.management.repository.UserRepository;
import com.iot.management.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final MailService mailService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       MailService mailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.mailService = mailService;
    }

    public String authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        String otp = mailService.generateOTP();
        user.setOtpCode(otp);
        user.setEnabled(false); 
        
        userRepository.save(user);
        
        try {
            mailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send email. Check your SMTP configuration.");
        }
    }

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));
        
        String otp = mailService.generateOTP();
        user.setOtpCode(otp);
        userRepository.save(user);
        
        try {
            mailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send email. Check your SMTP configuration.");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getOtpCode() != null && user.getOtpCode().equals(request.getOtpCode())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setOtpCode(null);
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid OTP Code");
        }
    }

    public void verifyAccount(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getOtpCode() != null && user.getOtpCode().equals(otpCode)) {
            user.setOtpCode(null);
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid OTP Code");
        }
    }
}
