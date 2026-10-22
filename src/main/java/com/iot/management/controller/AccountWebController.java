package com.iot.management.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller public class AccountWebController {
 @GetMapping("/account") public String account() {return "account";}
 @GetMapping("/management/users") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public String users() {return "users";}
 @GetMapping("/admin/settings") @PreAuthorize("hasRole('ADMIN')") public String admin() {return "admin";}
}
