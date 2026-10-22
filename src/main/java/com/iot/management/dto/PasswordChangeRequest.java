package com.iot.management.dto;
import javax.validation.constraints.*;
public class PasswordChangeRequest {
 @NotBlank public String currentPassword;
 @NotBlank @Size(min=6,max=72) public String newPassword;
}
