package com.iot.management.entity;
import javax.persistence.*;
import javax.validation.constraints.*;
@Entity @Table(name="suppliers")
public class Supplier {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank @Size(max=100) @Column(nullable=false,unique=true,length=100) public String name;
 @Size(max=200) public String contact;
 @Size(max=500) @Column(length=500) public String description;
}
