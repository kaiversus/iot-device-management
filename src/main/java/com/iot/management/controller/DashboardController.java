package com.iot.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.ui.Model model, org.springframework.security.core.Authentication authentication) {
        model.addAttribute("dashboardAdmin", authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return "dashboard";
    }
}
