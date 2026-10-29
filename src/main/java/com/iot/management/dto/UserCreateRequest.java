package com.iot.management.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserCreateRequest {
 @NotBlank @Email @Size(max=100) public String email;
 @NotBlank @Size(max=100) public String fullName;
 @NotBlank @Size(min=6,max=72) public String password;
 @Size(max=80) public String region;
 @NotBlank @Pattern(regexp="ROLE_USER|ROLE_MANAGER|ROLE_ADMIN") public String role;
}
