package com.iot.management;
import com.iot.management.entity.*;
import com.iot.management.repository.*;
import com.iot.management.security.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class IotIntegrationTest {
 @Autowired MockMvc mvc;
 @org.springframework.boot.test.mock.mockito.SpyBean org.springframework.messaging.simp.SimpMessagingTemplate messaging;
 @Autowired DeviceRepository devices;
 @Autowired DeviceDataRepository data;
 @Autowired DeviceLogRepository logs;
 @Autowired UserRepository users;
 @Autowired RoleRepository roles;
 @Autowired CategoryRepository categories;
 CustomUserDetails owner, other, admin; Long id;
 CustomUserDetails actor(String name,String role) {
  User u=new User(); u.setEmail(name+"@iot.test"); u.setPassword("unused");u.setFullName(name);u.setRoles(Set.of(roles.findByName(role).orElseThrow()));
  return new CustomUserDetails(users.save(u));
 }
 @BeforeEach void setup() {
  logs.deleteAll();data.deleteAll();devices.deleteAll();categories.deleteAll();users.deleteAll();
  owner=actor("owner","ROLE_USER");other=actor("other","ROLE_USER");admin=actor("admin","ROLE_ADMIN");
  Category c=new Category();c.setName("IoT");c=categories.save(c);
  Device d=new Device();d.setName("ESP32");d.setSerialNumber("DANG-1");d.setCategory(c);d.setOwner(owner.getUser());d.setStatus(DeviceStatus.ACTIVE);id=devices.save(d).getId();
 }
 @AfterEach void cleanup() {logs.deleteAll();data.deleteAll();}
 String payload(double temperature) {return "{\"deviceId\":"+id+",\"temperature\":"+temperature+",\"humidity\":65}";}
 void send(double temperature) throws Exception {
  mvc.perform(post("/api/iot/data").with(user(owner)).with(csrf()).contentType("application/json").content(payload(temperature))).andExpect(status().isOk());
 }
 @Test void savesHistoryAndAlertsOnlyAboveThresholdWithCooldown() throws Exception {
  send(50); assertThat(logs.count()).isZero();
  org.mockito.Mockito.verify(messaging).convertAndSendToUser(org.mockito.ArgumentMatchers.eq(owner.getUsername()),org.mockito.ArgumentMatchers.eq("/queue/data"),org.mockito.ArgumentMatchers.any(Object.class));send(51);send(60);
  assertThat(data.count()).isEqualTo(3);assertThat(logs.count()).isEqualTo(1);
  mvc.perform(get("/api/iot/data").with(user(owner))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
  mvc.perform(get("/api/iot/stats").with(user(owner))).andExpect(jsonPath("totalDevices").value(1)).andExpect(jsonPath("activeDevices").value(1)).andExpect(jsonPath("totalAlerts").value(1));
 }
 @Test void enforcesAuthenticationOwnershipAndValidation() throws Exception {
  mvc.perform(post("/api/iot/data").contentType("application/json").content(payload(51))).andExpect(status().isForbidden());
  mvc.perform(post("/api/iot/data").with(user(other)).with(csrf()).contentType("application/json").content(payload(51))).andExpect(status().isNotFound());
  mvc.perform(post("/api/iot/data").with(user(owner)).with(csrf()).contentType("application/json").content(payload(151))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/iot/data").with(user(owner)).with(csrf()).contentType("application/json").content("{\"deviceId\":999999,\"temperature\":25,\"humidity\":60}")).andExpect(status().isNotFound());
  send(60);
  mvc.perform(get("/api/iot/logs").with(user(owner))).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString("Cảnh báo:")));
  mvc.perform(get("/api/iot/data").with(user(other))).andExpect(content().json("[]"));
  mvc.perform(get("/api/iot/logs").with(user(other))).andExpect(content().json("[]"));
  mvc.perform(get("/api/iot/data").param("deviceId",id.toString()).with(user(other))).andExpect(status().isNotFound());
  mvc.perform(get("/api/iot/stats").with(user(admin))).andExpect(jsonPath("totalRecords").value(1));
 }
 @Test void dashboardRendersOwnerAndAdminSubscriptions() throws Exception {
  mvc.perform(get("/dashboard").with(user(owner))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("data-admin=\"false\"")));
  mvc.perform(get("/dashboard").with(user(admin))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("data-admin=\"true\"")));
 }
}
