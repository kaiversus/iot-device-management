package com.iot.management.controller;
import com.iot.management.dto.*;
import com.iot.management.service.UserManagementService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import javax.validation.Valid;
@RestController @RequestMapping("/api/management/users") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ManagementController {
 private final UserManagementService service;
 public ManagementController(UserManagementService s) {service=s;}
 @GetMapping public Page<UserResponse> list(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int page) {return service.list(q,page);}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 public UserResponse create(@Valid @RequestBody UserCreateRequest r) {return service.create(r);}
 @PutMapping("/{id}") public UserResponse update(@PathVariable Long id,@Valid @RequestBody UserUpdateRequest r) {return service.update(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void delete(@PathVariable Long id) {service.delete(id);}
}
