package com.iot.management.config;

import com.iot.management.security.CustomUserDetailsService;
import com.iot.management.security.JwtAuthenticationEntryPoint;
import com.iot.management.security.JwtAuthenticationFilter;
import com.iot.management.security.JwtTokenProvider;
import com.iot.management.security.DeviceCsrfMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtTokenProvider tokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                    .and()
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // JWT is authenticated on every request, including static assets.
                    // Do not rotate the CSRF cookie as if each request were a new login.
                    .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                    .requireCsrfProtectionMatcher(new DeviceCsrfMatcher())
                    .and()
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, exception) -> {
                        String path = request.getRequestURI().substring(request.getContextPath().length());
                        if (path.equals("/devices") || path.startsWith("/devices/")
                                || path.equals("/categories") || path.startsWith("/categories/")) {
                            response.sendRedirect(request.getContextPath() + "/login");
                        } else {
                            unauthorizedHandler.commence(request, response, exception);
                        }
                    })
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers("/api/auth/**").permitAll()
                    // --- Module IoT realtime (Đăng) ---
                    .antMatchers("/api/iot/**").authenticated()
                    .antMatchers("/ws/**").authenticated()
                    .antMatchers("/", "/login", "/register", "/forgot-password", "/css/**", "/js/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .antMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                    .antMatchers("/categories/**").hasAnyRole("ADMIN", "MANAGER")
                    .antMatchers("/devices/**", "/api/devices/**", "/api/categories/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                    .anyRequest().authenticated()
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .deleteCookies("jwt_token")
                    .logoutSuccessUrl("/login");

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
