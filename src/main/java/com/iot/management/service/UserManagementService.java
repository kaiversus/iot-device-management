package com.iot.management.service;
import com.iot.management.dto.*;
import com.iot.management.entity.*;
import com.iot.management.repository.*;
import com.iot.management.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @Transactional
public class UserManagementService {
 private final UserRepository users;private final RoleRepository roles;private final PasswordEncoder encoder;private final AuditService audit;private final UserDeletionService deletion;
 public UserManagementService(UserRepository u,RoleRepository r,PasswordEncoder e,AuditService a,UserDeletionService d) {users=u;roles=r;encoder=e;audit=a;deletion=d;}
 private CustomUserDetails actor() {return (CustomUserDetails)org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();}
 private boolean admin() {return actor().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));}
 public UserResponse me() {return UserResponse.from(users.findById(actor().getUser().getId()).orElseThrow());}
 public UserResponse profile(ProfileRequest r) {User u=users.lockByEmail(actor().getUsername()).orElseThrow();u.setFullName(r.fullName.trim());audit.record("PROFILE_UPDATE","user="+u.getId());return UserResponse.from(u);}
 public void password(PasswordChangeRequest r) {
  User u=users.lockByEmail(actor().getUsername()).orElseThrow();
  if(!encoder.matches(r.currentPassword,u.getPassword())) throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
  u.setPassword(encoder.encode(r.newPassword));u.setTokenVersion(u.getTokenVersion()+1);audit.record("PASSWORD_CHANGE","user="+u.getId());
 }
 public void revoke(String email) {users.lockByEmail(email).ifPresent(u->{u.setTokenVersion(u.getTokenVersion()+1);audit.record("LOGOUT","user="+u.getId());});}
 @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
 public Page<UserResponse> list(String q,int page) {
  if(page<0 || q.length()>100) throw new IllegalArgumentException("Bộ lọc không hợp lệ.");
  Specification<User> spec=(r,x,c)->c.isFalse(r.get("deleted"));
  if(!admin()) {
   String region=actor().getUser().getRegion();
   if(region==null || region.isBlank()) return Page.empty();
   spec=spec.and((r,x,c)->c.equal(r.get("region"),region));
  }
  if(!q.isBlank()) {String term="%"+q.trim().toLowerCase(Locale.ROOT).replace("%","\\%").replace("_","\\_")+"%";spec=spec.and((r,x,c)->c.or(c.like(c.lower(r.get("email")),term,'\\'),c.like(c.lower(r.get("fullName")),term,'\\')));}
  return users.findAll(spec,PageRequest.of(page,20,Sort.by("id"))).map(UserResponse::from);
 }
 @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
 public UserResponse create(UserCreateRequest r) {
  String email=r.email.trim().toLowerCase(Locale.ROOT);
  String region=r.region==null || r.region.isBlank()?null:r.region.trim();
  if(!admin()) {
   String scope=actor().getUser().getRegion();
   if(scope==null || scope.isBlank() || !scope.equals(region) || !r.role.equals("ROLE_USER"))
    throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Manager chỉ tạo User trong khu vực của mình.");
  }
  users.findByEmail(email).ifPresent(existing->{
   if(existing.isDeleted()) deletion.delete(existing);
   else throw new ResponseStatusException(HttpStatus.CONFLICT,"Email đã tồn tại.");
  });
  User u=new User();u.setEmail(email);u.setFullName(r.fullName.trim());u.setPassword(encoder.encode(r.password));
  u.setRegion(region);u.setRoles(Set.of(roles.findByName(r.role).orElseThrow()));
  u=users.save(u);audit.record("USER_CREATE","user="+u.getId()+", role="+r.role+", region="+region);
  return UserResponse.from(u);
 }
 @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
 public UserResponse update(Long id,UserUpdateRequest r) {
  User existing=users.findById(id).filter(u->!u.isDeleted()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  User u=users.lockByEmail(existing.getEmail()).orElseThrow();
  String region=r.region==null || r.region.isBlank()?null:r.region.trim();
  if(!admin()) {
   String scope=actor().getUser().getRegion();
   if(scope==null || !scope.equals(u.getRegion()) || !u.getRoles().stream().allMatch(role->role.getName().equals("ROLE_USER"))
      || !r.role.equals("ROLE_USER") || !Objects.equals(region,u.getRegion())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Manager chỉ sửa User trong khu vực, không đổi quyền/khu vực.");
  }
  if(u.getId().equals(actor().getUser().getId()) && (r.locked || !u.getRoles().stream().anyMatch(role->role.getName().equals(r.role)))) throw new IllegalArgumentException("Không được tự khóa hoặc thay quyền của mình.");
  // Protect other administrators; changes require the target to use their own account and a separate admin.
  if(u.getRoles().stream().anyMatch(role->role.getName().equals("ROLE_ADMIN")) && (!r.role.equals("ROLE_ADMIN") || r.locked)) throw new IllegalArgumentException("Không khóa/hạ quyền quản trị viên qua biểu mẫu này.");
  u.setFullName(r.fullName.trim());u.setRegion(region);u.setLocked(r.locked);u.setRoles(Set.of(roles.findByName(r.role).orElseThrow()));
  u.setTokenVersion(u.getTokenVersion()+1);audit.record("USER_UPDATE","user="+id+", role="+r.role+", locked="+r.locked+", region="+region);
  return UserResponse.from(u);
 }
 @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
 public void delete(Long id) {
  User existing=users.findById(id).filter(u->!u.isDeleted()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  User u=users.lockByEmail(existing.getEmail()).orElseThrow();
  if(u.getId().equals(actor().getUser().getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Không được tự xóa tài khoản của mình.");
  if(u.getRoles().stream().anyMatch(role->role.getName().equals("ROLE_ADMIN")))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Không được xóa tài khoản quản trị viên.");
  if(!admin()) {
   String scope=actor().getUser().getRegion();
   if(scope==null || !scope.equals(u.getRegion()) || !u.getRoles().stream().allMatch(role->role.getName().equals("ROLE_USER")))
    throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Manager chỉ xóa User trong khu vực của mình.");
  }
  deletion.delete(u);
  audit.record("USER_DELETE","user="+id);
 }
}
