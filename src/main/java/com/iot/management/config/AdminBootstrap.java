package com.iot.management.config;
import com.iot.management.entity.User;
import com.iot.management.repository.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class AdminBootstrap {
 @Bean @org.springframework.core.annotation.Order(10)
 CommandLineRunner bootstrap(UserRepository users,RoleRepository roles,PasswordEncoder encoder,
   @org.springframework.beans.factory.annotation.Value("${app.bootstrap.email:}") String email,
   @org.springframework.beans.factory.annotation.Value("${app.bootstrap.password:}") String password) {
  return args->{
   if(email.isBlank() || password.isBlank() || users.existsByEmail(email)) return;
   if(users.findAll().stream().anyMatch(u->u.getRoles().stream().anyMatch(r->r.getName().equals("ROLE_ADMIN")))) return;
   User u=new User();u.setEmail(email);u.setFullName("Quản trị viên");u.setPassword(encoder.encode(password));u.setEnabled(true);
   u.setRoles(java.util.Set.of(roles.findByName("ROLE_ADMIN").orElseThrow()));users.save(u);
  };
 }
}
