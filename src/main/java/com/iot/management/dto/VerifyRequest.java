package com.iot.management.dto;
import javax.validation.constraints.*;
public class VerifyRequest {
 @NotBlank @Email @Size(max=100) public String email;
 @NotBlank @Pattern(regexp="[0-9]{6}") public String otpCode;
}
