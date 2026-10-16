package com.iot.management.controller;

import com.iot.management.dto.CategoryRequest;
import com.iot.management.entity.Category;
import com.iot.management.service.CategoryService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @GetMapping public List<Category> list() { return service.list(); }
    @PostMapping public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        Category category = service.save(null, request);
        return ResponseEntity.created(URI.create("/api/categories/" + category.getId())).body(category);
    }
    @GetMapping("/{id}") public Category get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return service.save(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
