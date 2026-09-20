package com.iot.management.config;

import com.iot.management.entity.Role;
import com.iot.management.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.asList("ROLE_GUEST", "ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN").forEach(roleName -> {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                }
            });
        };
    }
}
