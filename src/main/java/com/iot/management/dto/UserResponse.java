package com.iot.management.dto;
import com.iot.management.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
public record UserResponse(Long id,String email,String fullName,String region,boolean enabled,boolean locked,Set<String> roles) {
 public static UserResponse from(User u) {return new UserResponse(u.getId(),u.getEmail(),u.getFullName(),u.getRegion(),u.isEnabled(),u.isLocked(),u.getRoles().stream().map(r->r.getName()).collect(Collectors.toSet()));}
}
