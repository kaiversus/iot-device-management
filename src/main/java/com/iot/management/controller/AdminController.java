package com.iot.management.controller;
import com.iot.management.entity.*;
import com.iot.management.repository.AuditLogRepository;
import com.iot.management.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 private final AuditLogRepository audit;private final SupplierService suppliers;private final SystemSettingsService settings;
 public AdminController(AuditLogRepository a,SupplierService s,SystemSettingsService t) {audit=a;suppliers=s;settings=t;}
 @GetMapping("/logs") public Page<AuditLog> logs(@RequestParam(defaultValue="0") int page) {
  if(page<0) throw new IllegalArgumentException("Trang không hợp lệ");return audit.findAll(PageRequest.of(page,30,Sort.by(Sort.Direction.DESC,"id")));
 }
 @GetMapping("/suppliers") public List<Supplier> suppliers() {return suppliers.list();}
 @PostMapping("/suppliers") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Supplier create(@Valid @RequestBody Supplier s) {return suppliers.save(null,s);}
 @PutMapping("/suppliers/{id}") public Supplier update(@PathVariable Long id,@Valid @RequestBody Supplier s) {return suppliers.save(id,s);}
 @DeleteMapping("/suppliers/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) {suppliers.delete(id);}
 public static class ThresholdRequest { @NotNull @DecimalMin("-50") @DecimalMax("150") public Double threshold; }
 @GetMapping("/settings") public Map<String,Double> settings() {return Map.of("threshold",settings.threshold());}
 @PutMapping("/settings") public Map<String,Double> settings(@Valid @RequestBody ThresholdRequest r) {settings.threshold(r.threshold);return settings();}
}
