package com.iot.management.service;
import com.iot.management.entity.Supplier;
import com.iot.management.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service @PreAuthorize("hasRole('ADMIN')") @Transactional
public class SupplierService {
 private final SupplierRepository repo;private final AuditService audit;
 public SupplierService(SupplierRepository r,AuditService a) {repo=r;audit=a;}
 public java.util.List<Supplier> list() {return repo.findAll(org.springframework.data.domain.Sort.by("name"));}
 public Supplier save(Long id,Supplier r) {
  Supplier s=id==null?new Supplier():repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  s.name=r.name.trim();s.contact=r.contact;s.description=r.description;
  s=repo.saveAndFlush(s);audit.record("SUPPLIER_SAVE","supplier="+s.id);return s;
 }
 public void delete(Long id) {Supplier s=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));repo.delete(s);audit.record("SUPPLIER_DELETE","supplier="+id);}
}
