package com.iot.management.dto;
import javax.validation.constraints.*;
public class ProfileRequest { @NotBlank @Size(max=100) public String fullName; }
