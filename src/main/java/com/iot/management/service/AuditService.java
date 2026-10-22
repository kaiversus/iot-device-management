package com.iot.management.service;
import com.iot.management.entity.AuditLog;
import com.iot.management.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
@Service public class AuditService {
 private final AuditLogRepository logs;
 public AuditService(AuditLogRepository logs) {this.logs=logs;}
 public void record(String action,String detail) {
  var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
  logs.save(new AuditLog(a==null?"system":a.getName(),action,detail));
 }
}
