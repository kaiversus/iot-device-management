package com.iot.management.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 100)
    private String otpCode;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    private String region = null;
    public String getRegion() { return region; }
    public void setRegion(String v) { region=v; }
    private boolean locked = false;
    public boolean isLocked() { return locked; }
    public void setLocked(boolean v) { locked=v; }
    @Column(nullable = false)
    private boolean deleted = false;
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean v) { deleted=v; }
    private Long tokenVersion = 0L;
    public Long getTokenVersion() { return tokenVersion == null ? 0L : tokenVersion; }
    public void setTokenVersion(Long v) { tokenVersion=v; }
    private java.time.Instant otpExpiresAt = null;
    public java.time.Instant getOtpExpiresAt() { return otpExpiresAt; }
    public void setOtpExpiresAt(java.time.Instant v) { otpExpiresAt=v; }
    private java.time.Instant otpSentAt = null;
    public java.time.Instant getOtpSentAt() { return otpSentAt; }
    public void setOtpSentAt(java.time.Instant v) { otpSentAt=v; }
    private String otpPurpose = null;
    public String getOtpPurpose() { return otpPurpose; }
    public void setOtpPurpose(String v) { otpPurpose=v; }
    private int otpAttempts = 0;
    public int getOtpAttempts() { return otpAttempts; }
    public void setOtpAttempts(int v) { otpAttempts=v; }
    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
