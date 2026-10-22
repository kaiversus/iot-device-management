package com.iot.management.entity;
import javax.persistence.*;
import java.time.Instant;
@Entity @Table(name="system_audit_logs")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 @Column(nullable=false) public String actor;
 @Column(nullable=false) public String action;
 @Column(length=500) public String detail;
 public AuditLog() {}
 public AuditLog(String actor,String action,String detail) {this.actor=actor;this.action=action;this.detail=detail;}
}
