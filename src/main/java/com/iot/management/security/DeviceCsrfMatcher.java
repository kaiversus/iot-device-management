package com.iot.management.security;

import org.springframework.security.web.util.matcher.RequestMatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;

/** Protect the new cookie-authenticated forms/APIs without changing the existing auth module. */
public class DeviceCsrfMatcher implements RequestMatcher {
    @Override public boolean matches(HttpServletRequest request) {
        if (Set.of("GET", "HEAD", "OPTIONS", "TRACE").contains(request.getMethod())) return false;
        boolean module = Arrays.stream(new String[]{"/devices", "/categories", "/api/devices", "/api/categories", "/api/iot"})
            .anyMatch(prefix -> requestPath(request).equals(prefix) || requestPath(request).startsWith(prefix + "/"));
        if (!module) return false;
        String authorization = request.getHeader("Authorization");
        boolean jwtCookie = request.getCookies() != null && Arrays.stream(request.getCookies()).anyMatch(c -> c.getName().equals("jwt_token"));
        return jwtCookie || authorization == null || !authorization.startsWith("Bearer ");
    }
    private String requestPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
