package com.iot.management.service;
import com.iot.management.entity.SystemSetting;
import com.iot.management.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
@Service public class SystemSettingsService {
 private final SystemSettingRepository repo;private final AuditService audit;private final double fallback;
 public SystemSettingsService(SystemSettingRepository r,AuditService a,@org.springframework.beans.factory.annotation.Value("${iot.temperature-threshold:50}") double f) {repo=r;audit=a;fallback=f;}
 public double threshold() {return repo.findById("temperature-threshold").map(s->Double.parseDouble(s.value)).orElse(fallback);}
 @Transactional @PreAuthorize("hasRole('ADMIN')") public void threshold(double value) {
  if(!Double.isFinite(value) || value< -50 || value>150) throw new IllegalArgumentException("Ngưỡng phải từ -50 đến 150°C.");
  repo.save(new SystemSetting("temperature-threshold",Double.toString(value)));audit.record("SETTINGS_UPDATE","temperature-threshold="+value);
 }
}
