package com.iot.management.security;
import org.springframework.security.web.util.matcher.RequestMatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
/** Cookie-authenticated writes require CSRF; SockJS is protected by same-origin. */
public class DeviceCsrfMatcher implements RequestMatcher {
 public boolean matches(HttpServletRequest r) {
  if(Set.of("GET","HEAD","OPTIONS","TRACE").contains(r.getMethod())) return false;
  String path=r.getRequestURI().substring(r.getContextPath().length());
  if(path.startsWith("/ws/")) return false;
  boolean cookie=r.getCookies()!=null && Arrays.stream(r.getCookies()).anyMatch(c -> c.getName().equals("jwt_token"));
  String auth=r.getHeader("Authorization");
  return cookie || auth==null || !auth.startsWith("Bearer ");
 }
}
