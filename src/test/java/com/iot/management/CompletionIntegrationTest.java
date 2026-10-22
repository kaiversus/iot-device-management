package com.iot.management;
import com.iot.management.entity.*;
import com.iot.management.repository.*;
import com.iot.management.security.*;
import com.iot.management.service.MailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CompletionIntegrationTest {
 @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired RoleRepository roles;
 @Autowired DeviceRepository devices; @Autowired CategoryRepository categories; @Autowired DeviceDataRepository data; @Autowired DeviceLogRepository logs;
 @Autowired AuditLogRepository audit; @Autowired SystemSettingRepository settings; @Autowired SupplierRepository suppliers;
 @Autowired PasswordEncoder encoder; @Autowired JwtTokenProvider tokens; @MockBean MailService mail;
 User owner,manager,other,admin;
 @BeforeEach void setup() {
  logs.deleteAll();data.deleteAll();devices.deleteAll();categories.deleteAll();users.deleteAll();settings.deleteAll();suppliers.deleteAll();audit.deleteAll();
  owner=actor("owner","ROLE_USER","North");manager=actor("manager","ROLE_MANAGER","North");other=actor("other","ROLE_USER","South");admin=actor("admin","ROLE_ADMIN",null);
  when(mail.generateOTP()).thenReturn("123456");
 }
 @AfterEach void cleanup() {logs.deleteAll();data.deleteAll();settings.deleteAll();}
 User actor(String name,String role,String region) {
  User u=new User();u.setEmail(name+"@test.invalid");u.setFullName(name);u.setPassword(encoder.encode("oldpass123"));u.setRegion(region);u.setRoles(Set.of(roles.findByName(role).orElseThrow()));return users.save(u);
 }
 String token(User u) {CustomUserDetails p=new CustomUserDetails(u);return tokens.generateToken(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(p,null,p.getAuthorities()));}
 org.springframework.test.web.servlet.request.RequestPostProcessor as(User u) {return user(new CustomUserDetails(u));}
 void issue(String purpose,Instant expiry) {owner.setOtpCode(encoder.encode("123456"));owner.setOtpPurpose(purpose);owner.setOtpExpiresAt(expiry);owner=users.save(owner);}
 String resetBody(String code) {return "{\"email\":\"owner@test.invalid\",\"otpCode\":\""+code+"\",\"newPassword\":\"newpass123\"}";}
 String updateBody(String region,String role) {return "{\"fullName\":\"Changed\",\"region\":\""+region+"\",\"role\":\""+role+"\",\"locked\":false}";}
 @Test void mailFailureIs503AndDoesNotPersistRegistration() throws Exception {
  doThrow(new RuntimeException("offline")).when(mail).sendOtpEmail(anyString(),anyString());
  mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{\"email\":\"new@test.invalid\",\"fullName\":\"New\",\"password\":\"pass1234\"}")).andExpect(status().isServiceUnavailable());
  assertThat(users.existsByEmail("new@test.invalid")).isFalse();
  mvc.perform(post("/api/auth/forgot-password").with(csrf()).contentType("application/json").content("{\"email\":\"owner@test.invalid\"}")).andExpect(status().isServiceUnavailable());
 }
 @Test void registrationVerificationAndLoginWorkWithHashedOtp() throws Exception {
  mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{\"email\":\"new@test.invalid\",\"fullName\":\"New\",\"password\":\"pass1234\"}")).andExpect(status().isOk());
  User u=users.findByEmail("new@test.invalid").orElseThrow();assertThat(u.isEnabled()).isFalse();assertThat(u.getOtpCode()).isNotEqualTo("123456");
  mvc.perform(post("/api/auth/verify-account").with(csrf()).contentType("application/json").content("{\"email\":\"new@test.invalid\",\"otpCode\":\"123456\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{\"email\":\"new@test.invalid\",\"password\":\"pass1234\"}")).andExpect(status().isOk()).andExpect(header().string("Set-Cookie",org.hamcrest.Matchers.containsString("HttpOnly")));
 }
 @Test void expiryPurposeAndFiveAttemptsAreEnforced() throws Exception {
  issue("VERIFY",Instant.now().plusSeconds(300));
  mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType("application/json").content(resetBody("123456"))).andExpect(status().isBadRequest());
  issue("RESET",Instant.now().minusSeconds(1));
  mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType("application/json").content(resetBody("123456"))).andExpect(status().isBadRequest());
  issue("RESET",Instant.now().plusSeconds(300));
  for(int i=0;i<5;i++) mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType("application/json").content(resetBody("000000"))).andExpect(status().isBadRequest());
  assertThat(users.findById(owner.getId()).orElseThrow().getOtpAttempts()).isEqualTo(5);
  mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType("application/json").content(resetBody("123456"))).andExpect(status().isBadRequest());
 }
 @Test void resendCooldownAndAuthCsrfAreEnforced() throws Exception {
  mvc.perform(post("/api/auth/forgot-password").contentType("application/json").content("{\"email\":\"owner@test.invalid\"}")).andExpect(status().isForbidden());
  for(int i=0;i<2;i++) mvc.perform(post("/api/auth/forgot-password").with(csrf()).contentType("application/json").content("{\"email\":\"owner@test.invalid\"}")).andExpect(i==0?status().isOk():status().isBadRequest());
  verify(mail,times(1)).sendOtpEmail(anyString(),anyString());
 }
 @Test void disabledLockedAndResetTokensAreRejected() throws Exception {
  String jwt=token(owner);owner.setEnabled(false);users.save(owner);
  mvc.perform(get("/api/devices").header("Authorization","Bearer "+jwt)).andExpect(status().isUnauthorized());
  owner.setEnabled(true);owner.setLocked(true);users.save(owner);
  mvc.perform(get("/api/devices").header("Authorization","Bearer "+jwt)).andExpect(status().isUnauthorized());
  owner.setLocked(false);issue("RESET",Instant.now().plusSeconds(300));
  mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType("application/json").content(resetBody("123456"))).andExpect(status().isOk());
  mvc.perform(get("/api/devices").header("Authorization","Bearer "+jwt)).andExpect(status().isUnauthorized());
 }
 @Test void logoutRevokesTokenAndProfileNeverExposesSecrets() throws Exception {
  String jwt=token(owner);
  mvc.perform(get("/api/account").header("Authorization","Bearer "+jwt)).andExpect(status().isOk()).andExpect(jsonPath("password").doesNotExist()).andExpect(jsonPath("otpCode").doesNotExist());
  mvc.perform(post("/logout").header("Authorization","Bearer "+jwt)).andExpect(status().is3xxRedirection());
  mvc.perform(get("/api/account").header("Authorization","Bearer "+jwt)).andExpect(status().isUnauthorized());
 }
 @Test void managerOnlyManagesAssignedUsersAndCannotEscalate() throws Exception {
  mvc.perform(get("/api/management/users").with(as(manager))).andExpect(status().isOk()).andExpect(jsonPath("totalElements").value(2));
  mvc.perform(get("/api/management/users").with(as(owner))).andExpect(status().isForbidden());
  mvc.perform(put("/api/management/users/{id}",owner.getId()).with(as(manager)).with(csrf()).contentType("application/json").content(updateBody("North","ROLE_USER"))).andExpect(status().isOk());
  mvc.perform(put("/api/management/users/{id}",other.getId()).with(as(manager)).with(csrf()).contentType("application/json").content(updateBody("South","ROLE_USER"))).andExpect(status().isForbidden());
  mvc.perform(put("/api/management/users/{id}",owner.getId()).with(as(manager)).with(csrf()).contentType("application/json").content(updateBody("North","ROLE_ADMIN"))).andExpect(status().isForbidden());
 }
 Device device(User owner,String serial) {Category c=new Category();c.setName(serial);c=categories.save(c);Device d=new Device();d.setName(serial);d.setSerialNumber(serial);d.setOwner(owner);d.setCategory(c);return devices.save(d);}
 @Test void managerReadsAreaButCannotSendOrDeleteOthersDevices() throws Exception {
  Device d=device(owner,"NORTH");device(other,"SOUTH");
  mvc.perform(get("/api/devices").with(as(manager))).andExpect(jsonPath("totalElements").value(1));
  mvc.perform(get("/api/iot/stats").with(as(manager))).andExpect(jsonPath("totalDevices").value(1));
  mvc.perform(get("/api/iot/data").param("deviceId",d.getId().toString()).with(as(manager))).andExpect(status().isOk());
  mvc.perform(post("/api/iot/data").with(as(manager)).with(csrf()).contentType("application/json").content("{\"deviceId\":"+d.getId()+",\"temperature\":60,\"humidity\":50}")).andExpect(status().isNotFound());
  mvc.perform(delete("/api/devices/{id}",d.getId()).with(as(manager)).with(csrf())).andExpect(status().isNotFound());
 }
 @Test void deviceHistoryPreventsDeletionAndDatabaseEnforcesForeignKey() throws Exception {
  Device d=device(owner,"HISTORY");DeviceData reading=new DeviceData();reading.setDeviceId(d.getId());reading.setTemperature(60d);reading.setHumidity(50d);data.save(reading);
  mvc.perform(delete("/api/devices/{id}",d.getId()).with(as(owner)).with(csrf())).andExpect(status().isConflict());
  assertThat(devices.existsById(d.getId())).isTrue();
  DeviceData invalid=new DeviceData();invalid.setDeviceId(999999L);
  assertThatThrownBy(()->data.saveAndFlush(invalid)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 }
 @Test void adminSettingsSuppliersAndAuditAreProtectedAndPersisted() throws Exception {
  mvc.perform(get("/api/admin/logs").with(as(manager))).andExpect(status().isForbidden());
  mvc.perform(put("/api/admin/settings").with(as(admin)).with(csrf()).contentType("application/json").content("{\"threshold\":65}")).andExpect(status().isOk());
  mvc.perform(get("/api/iot/stats").with(as(owner))).andExpect(jsonPath("threshold").value(65));
  mvc.perform(post("/api/admin/suppliers").with(as(admin)).with(csrf()).contentType("application/json").content("{\"name\":\"Supplier A\"}")).andExpect(status().isCreated());
  assertThat(audit.count()).isEqualTo(2);
  mvc.perform(get("/api/admin/logs").with(as(admin))).andExpect(jsonPath("totalElements").value(2));
 }
 @Test void newPagesRenderForTheirRoles() throws Exception {
  mvc.perform(get("/account").with(as(owner))).andExpect(status().isOk());
  mvc.perform(get("/management/users").with(as(manager))).andExpect(status().isOk());
  mvc.perform(get("/admin/settings").with(as(admin))).andExpect(status().isOk());
  mvc.perform(get("/admin/settings").with(as(owner))).andExpect(status().isForbidden());
 }
}
