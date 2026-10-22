package com.iot.management.entity;
import javax.persistence.*;
@Entity @Table(name="system_settings")
public class SystemSetting { @Id public String name; @Column(name="setting_value") public String value; public SystemSetting() {} public SystemSetting(String n,String v) {name=n;value=v;} }
