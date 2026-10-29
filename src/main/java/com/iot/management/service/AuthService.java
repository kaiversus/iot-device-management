package com.iot.management.service;
import com.iot.management.dto.*;
import com.iot.management.entity.*;
import com.iot.management.repository.*;
import com.iot.management.security.JwtTokenProvider;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(noRollbackFor = IllegalArgumentException.class)
public class AuthService {
 private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);
 private final AuthenticationManager authenticationManager;
 private final UserRepository users;
 private final RoleRepository roles;
 private final PasswordEncoder encoder;
 private final JwtTokenProvider tokens;
 private final MailService mail;
 private final UserDeletionService deletion;
 public AuthService(AuthenticationManager a, UserRepository u, RoleRepository r, PasswordEncoder e, JwtTokenProvider t, MailService m, UserDeletionService d) {
  authenticationManager=a;users=u;roles=r;encoder=e;tokens=t;mail=m;deletion=d;
 }
 public String authenticateUser(LoginRequest r) {
  Authentication a=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalize(r.getEmail()),r.getPassword()));
  return tokens.generateToken(a);
 }
 public void registerUser(RegisterRequest r) {
  String email=normalize(r.getEmail());
  users.findByEmail(email).ifPresent(existing->{
   if(existing.isDeleted()) deletion.delete(existing);
   else throw new IllegalArgumentException("Email đã đăng ký. Nếu chưa xác thực, hãy gửi lại OTP.");
  });
  User u=new User();u.setEmail(email);u.setFullName(r.getFullName().trim());u.setPassword(encoder.encode(r.getPassword()));
  u.setRoles(Set.of(roles.findByName("ROLE_USER").orElseThrow()));u.setEnabled(false);
  issue(u,"VERIFY");
 }
 public void resendVerification(String email) {
  User u=locked(email);
  if(u.isEnabled() || u.isLocked()) throw new IllegalArgumentException("Tài khoản không thể xác thực lại.");
  issue(u,"VERIFY");
 }
 public void processForgotPassword(String email) {
  User u=locked(email);
  if(!u.isEnabled() || u.isLocked()) throw new IllegalArgumentException("Tài khoản chưa xác thực hoặc đã bị khóa.");
  issue(u,"RESET");
 }
 private void issue(User u,String purpose) {
  Instant now=Instant.now();
  if(u.getOtpSentAt()!=null && u.getOtpSentAt().plusSeconds(60).isAfter(now)) throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi gửi lại OTP.");
  String code=mail.generateOTP();
  u.setOtpCode(encoder.encode(code));u.setOtpPurpose(purpose);u.setOtpExpiresAt(now.plusSeconds(300));u.setOtpSentAt(now);u.setOtpAttempts(0);
  users.saveAndFlush(u);
  try { mail.sendOtpEmail(u.getEmail(),code); }
  catch(RuntimeException ex) {
   log.error("OTP email delivery failed", ex);
   throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,"Không gửi được email. Vui lòng thử lại sau.");
  }
 }
 public void resetPassword(ResetPasswordRequest r) {
  User u=locked(r.getEmail());check(u,r.getOtpCode(),"RESET");
  if(!u.isEnabled() || u.isLocked()) throw new IllegalArgumentException("Tài khoản không khả dụng.");
  u.setPassword(encoder.encode(r.getNewPassword()));u.setTokenVersion(u.getTokenVersion()+1);clear(u);
 }
 public void verifyAccount(String email,String code) {
  User u=locked(email);check(u,code,"VERIFY");
  if(u.isLocked()) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
  u.setEnabled(true);clear(u);
 }
 private void check(User u,String code,String purpose) {
  if(u.getOtpCode()==null || !purpose.equals(u.getOtpPurpose()) || u.getOtpExpiresAt()==null || !u.getOtpExpiresAt().isAfter(Instant.now()) || u.getOtpAttempts()>=5)
   throw new IllegalArgumentException("OTP hết hạn, sai mục đích hoặc đã vượt số lần thử. Hãy gửi lại mã.");
  if(code==null || !code.matches("[0-9]{6}") || !encoder.matches(code,u.getOtpCode())) {
   u.setOtpAttempts(u.getOtpAttempts()+1);users.saveAndFlush(u);
   throw new IllegalArgumentException("OTP không đúng.");
  }
 }
 private void clear(User u) {u.setOtpCode(null);u.setOtpPurpose(null);u.setOtpExpiresAt(null);u.setOtpAttempts(0);}
 private User locked(String email) {return users.lockByEmail(normalize(email)).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));}
 private String normalize(String email) {return email==null ? "" : email.trim().toLowerCase(Locale.ROOT);}
}
