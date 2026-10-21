package com.iot.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.management.entity.*;
import com.iot.management.repository.*;
import com.iot.management.security.CustomUserDetails;
import com.iot.management.security.JwtTokenProvider;
import com.iot.management.service.CloudinaryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class DeviceIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @SpyBean DeviceRepository devices;
    @Autowired JwtTokenProvider tokens;
    @Autowired CategoryRepository categories;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @MockBean CloudinaryService images;
    CustomUserDetails owner, other, admin, manager;
    Category category;

    @BeforeEach void setup() {
        devices.deleteAll(); categories.deleteAll(); users.deleteAll();
        owner = actor("owner", "ROLE_USER"); other = actor("other", "ROLE_USER");
        admin = actor("admin", "ROLE_ADMIN"); manager = actor("manager", "ROLE_MANAGER");
        category = new Category(); category.setName("Sensors"); category = categories.save(category);
        when(images.isConfigured()).thenReturn(true);
    }
    private CustomUserDetails actor(String name, String role) {
        User u = new User(); u.setEmail(name + "@example.test"); u.setPassword("unused"); u.setFullName(name);
        u.setRoles(Set.of(roles.findByName(role).orElseThrow())); return new CustomUserDetails(users.save(u));
    }
    private String body(String name, String serial) throws Exception {
        return json.writeValueAsString(Map.of("name", name, "serialNumber", serial, "categoryId", category.getId(), "status", "ACTIVE"));
    }
    private long create(String name, String serial, CustomUserDetails actor) throws Exception {
        String response = mvc.perform(post("/api/devices").with(user(actor)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(body(name, serial)))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(jsonPath("password").doesNotExist()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }
    @Test void crudAndOwnershipAreEnforced() throws Exception {
        long id = create("Sensor", "S-01", owner);
        mvc.perform(get("/api/devices/{id}", id).with(user(owner))).andExpect(status().isOk());
        mvc.perform(get("/api/devices/{id}", id).with(user(other))).andExpect(status().isNotFound());
        mvc.perform(put("/api/devices/{id}", id).with(user(other)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body("Stolen", "S-01"))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/devices/{id}", id).with(user(manager)).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(put("/api/devices/{id}", id).with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body("Updated", "S-01"))).andExpect(status().isOk()).andExpect(jsonPath("name").value("Updated"));
        mvc.perform(delete("/api/devices/{id}", id).with(user(admin)).with(csrf())).andExpect(status().isNoContent());
        assertThat(devices.existsById(id)).isFalse();
    }
    @Test void searchFiltersPaginationAndAdminScope() throws Exception {
        create("Kitchen sensor", "K-1", owner); create("Kitchen spare", "K-2", owner); create("Other kitchen", "K-3", other);
        mvc.perform(get("/api/devices").with(user(owner)).param("q", "kItChEn").param("categoryId", category.getId().toString()).param("status", "ACTIVE").param("size", "1").param("page", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("totalElements").value(2)).andExpect(jsonPath("content", hasSize(1)));
        mvc.perform(get("/api/devices").with(user(admin))).andExpect(jsonPath("totalElements").value(3));
        mvc.perform(get("/api/devices").with(user(owner)).param("status", "INACTIVE")).andExpect(jsonPath("totalElements").value(0));
        mvc.perform(get("/api/devices").with(user(owner)).param("q", "%")).andExpect(jsonPath("totalElements").value(0));
        mvc.perform(get("/api/devices").with(user(owner)).param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/devices").with(user(owner)).param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/devices").with(user(owner)).param("status", "FAKE")).andExpect(status().isBadRequest());
    }
    @Test void invalidAndDuplicateInputIsRejected() throws Exception {
        create("Sensor", "s-01", owner);
        mvc.perform(post("/api/devices").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body("Duplicate", "S-01"))).andExpect(status().isConflict());
        mvc.perform(post("/api/devices").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body("  ", "BAD"))).andExpect(status().isBadRequest()).andExpect(jsonPath("fields.name").exists());
        mvc.perform(post("/api/devices").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Missing\"}" )).andExpect(status().isBadRequest());
    }
    @Test void categoryWritesRequireManagerAndUsedCategoryCannotBeDeleted() throws Exception {
        String categoryJson = "{\"name\":\"Lighting\",\"description\":\"Lights\"}";
        mvc.perform(post("/api/categories").with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(categoryJson)).andExpect(status().isForbidden());
        mvc.perform(post("/api/categories").with(user(manager)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(categoryJson)).andExpect(status().isCreated());
        mvc.perform(post("/api/categories").with(user(manager)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(categoryJson)).andExpect(status().isConflict());
        create("Sensor", "S-01", owner);
        mvc.perform(delete("/api/categories/{id}", category.getId()).with(user(admin)).with(csrf())).andExpect(status().isConflict());
        mvc.perform(put("/api/categories/{id}", category.getId()).with(user(manager)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Renamed\"}")).andExpect(status().isOk());
        devices.deleteAll();
        mvc.perform(delete("/api/categories/{id}", category.getId()).with(user(manager)).with(csrf())).andExpect(status().isNoContent());
    }
    @Test void templatesRenderAndWebFormsWork() throws Exception {
        snapshot("home", mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        for (String path : List.of("/login", "/register", "/forgot-password")) mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get("/devices").with(user(owner))).andExpect(status().isOk()).andExpect(view().name("devices/list"));
        snapshot("form", mvc.perform(get("/devices/new").with(user(owner))).andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\""))).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        long id = create("<script>alert(1)</script>", "S-01", owner);
        create("Second", "S-02", owner);
        snapshot("devices", mvc.perform(get("/devices").with(user(owner)).param("size", "1")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        mvc.perform(get("/devices/{id}", id).with(user(owner))).andExpect(status().isOk()).andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        mvc.perform(get("/devices/{id}/edit", id).with(user(owner))).andExpect(status().isOk());
        mvc.perform(get("/devices/{id}", id).with(user(other))).andExpect(status().isNotFound()).andExpect(view().name("devices/error"));
        mvc.perform(get("/categories").with(user(manager))).andExpect(status().isOk());
        mvc.perform(get("/categories/{id}/edit", category.getId()).with(user(manager))).andExpect(status().isOk());
        mvc.perform(post("/categories").with(user(manager)).with(csrf()).param("name", "From web")).andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/devices").with(user(owner)).with(csrf()).param("name", "Web device").param("serialNumber", "WEB-1").param("categoryId", category.getId().toString()).param("status", "ACTIVE")).andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/devices").with(user(owner)).with(csrf()).param("name", "")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("deviceForm", "name"));
    }
    @Test void unauthenticatedAndCsrfRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/devices")).andExpect(status().isUnauthorized());
        mvc.perform(get("/devices")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/api/devices").with(user(owner)).contentType(MediaType.APPLICATION_JSON).content(body("No csrf", "X-1"))).andExpect(status().isForbidden());
    }
    @Test void imageReplacementAndRemovalCleanUpAfterCommit() throws Exception {
        when(images.upload(any())).thenReturn(new CloudinaryService.UploadedImage("https://res.cloudinary.com/demo/image/upload/new.png", "new-image"));
        long id = create("Sensor", "S-01", owner);
        Device d = devices.findById(id).orElseThrow(); d.setImageUrl("https://example.test/old.png"); d.setImagePublicId("old-image"); devices.save(d);
        MockMultipartFile file = new MockMultipartFile("image", "device.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/api/devices/{id}/image", id).file(file).with(user(other)).with(csrf())).andExpect(status().isNotFound());
        verify(images, never()).upload(any());
        mvc.perform(multipart("/api/devices/{id}/image", id).file(file).with(user(owner)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("imageUrl").value(containsString("new.png")));
        verify(images).deleteQuietly("old-image");
        String remove = body("Sensor", "S-01").replace("}", ",\"removeImage\":true}");
        mvc.perform(put("/api/devices/{id}", id).with(user(owner)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(remove)).andExpect(status().isOk()).andExpect(jsonPath("imageUrl").isEmpty());
        verify(images).deleteQuietly("new-image");
    }

    @Test void realJwtWorksForCookieAndBearerAndCookieWritesNeedCsrf() throws Exception {
        String token = tokens.generateToken(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(owner, null, owner.getAuthorities()));
        mvc.perform(get("/api/devices").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/devices").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body("Bearer", "B-1"))).andExpect(status().isCreated());
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("jwt_token", token);
        mvc.perform(get("/devices").cookie(cookie)).andExpect(status().isOk());
        mvc.perform(post("/api/devices").cookie(cookie).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body("Cookie", "C-1"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/devices").cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body("Cookie", "C-1"))).andExpect(status().isCreated());
    }

    @Test void databaseFailureRemovesNewUploadAndPreservesExistingImage() throws Exception {
        long id = create("Sensor", "S-01", owner);
        Device d = devices.findById(id).orElseThrow(); d.setImagePublicId("original"); d.setImageUrl("https://example.test/original.png"); devices.save(d);
        when(images.upload(any())).thenReturn(new CloudinaryService.UploadedImage("https://example.test/new.png", "rollback-image"));
        doThrow(new org.springframework.dao.DataIntegrityViolationException("Simulated write failure")).when(devices).saveAndFlush(any(Device.class));
        mvc.perform(multipart("/api/devices/{id}/image", id).file(new MockMultipartFile("image", new byte[]{1})).with(user(owner)).with(csrf()))
            .andExpect(status().isConflict());
        verify(images).deleteQuietly("rollback-image");
        verify(images, never()).deleteQuietly("original");
        assertThat(devices.findById(id).orElseThrow().getImagePublicId()).isEqualTo("original");
    }
    // Mock csrf() used by other tests replaces the repository; exercise the real cookie repository here.
    @org.springframework.test.annotation.DirtiesContext(methodMode = org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test void jwtAssetRequestsPreserveCsrfAndMultipartSaveWorks() throws Exception {
        String jwt = tokens.generateToken(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(owner, null, owner.getAuthorities()));
        Map<String, javax.servlet.http.Cookie> jar = new HashMap<>();
        jar.put("jwt_token", new javax.servlet.http.Cookie("jwt_token", jwt));
        String formToken = null;
        for (String path : List.of("/devices/new", "/css/app.css", "/js/devices.js")) {
            var response = mvc.perform(get(path).cookie(jar.values().toArray(new javax.servlet.http.Cookie[0])))
                .andExpect(status().isOk()).andReturn().getResponse();
            for (var cookie : response.getCookies()) {
                if (cookie.getMaxAge() == 0) jar.remove(cookie.getName());
                else jar.put(cookie.getName(), cookie);
            }
            if (formToken == null) {
                var matcher = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(response.getContentAsString());
                assertThat(matcher.find()).isTrue();
                formToken = matcher.group(1);
            }
            assertThat(jar.get("XSRF-TOKEN").getValue()).isEqualTo(formToken);
        }
        mvc.perform(multipart("/devices").cookie(jar.values().toArray(new javax.servlet.http.Cookie[0]))
            .header("X-XSRF-TOKEN", formToken).param("_csrf", formToken)
            .param("name", "Browser device").param("serialNumber", "BROWSER-1")
            .param("categoryId", category.getId().toString()).param("status", "ACTIVE"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("/devices/*"));
        assertThat(devices.findAll()).extracting(Device::getSerialNumber).containsExactly("BROWSER-1");
    }

    private void snapshot(String name, String html) throws Exception {
        java.nio.file.Path directory = java.nio.file.Path.of("target", "ui-preview");
        java.nio.file.Files.createDirectories(directory);
        java.nio.file.Files.writeString(directory.resolve(name + ".html"), html);
    }
}
