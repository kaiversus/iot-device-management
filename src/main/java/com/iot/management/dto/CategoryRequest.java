package com.iot.management.dto;

import javax.validation.constraints.*;

public class CategoryRequest {
    @NotBlank(message = "Vui lòng nhập tên danh mục") @Size(max = 100)
    private String name;
    @Size(max = 500)
    private String description;
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? null : name.trim(); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
