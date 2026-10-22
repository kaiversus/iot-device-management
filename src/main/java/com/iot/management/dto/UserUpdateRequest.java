package com.iot.management.dto;
import javax.validation.constraints.*;
public class UserUpdateRequest {
 @NotBlank @Size(max=100) public String fullName;
 @Size(max=80) public String region;
 @NotBlank @Pattern(regexp="ROLE_USER|ROLE_MANAGER|ROLE_ADMIN") public String role;
 public boolean locked;
}
