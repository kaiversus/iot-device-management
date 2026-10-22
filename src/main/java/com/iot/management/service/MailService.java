package com.iot.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public String generateOTP() {
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        int number = rnd.nextInt(1000000);
        return String.format("%06d", number);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code - IoT Device Management System");
        message.setText("Your OTP code is: " + otp + "\n\nPlease use this code to complete your action.");

        mailSender.send(message);
    }
}
