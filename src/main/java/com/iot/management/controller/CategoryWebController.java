package com.iot.management.controller;

import com.iot.management.dto.CategoryRequest;
import com.iot.management.entity.Category;
import com.iot.management.exception.DeviceException;
import com.iot.management.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;

@Controller
@RequestMapping("/categories")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class CategoryWebController {
    private final CategoryService service;
    public CategoryWebController(CategoryService service) { this.service = service; }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categoryForm", new CategoryRequest()); return page(model, null);
    }
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Category c = service.get(id); CategoryRequest request = new CategoryRequest();
        request.setName(c.getName()); request.setDescription(c.getDescription());
        model.addAttribute("categoryForm", request); return page(model, id);
    }
    @PostMapping({"", "/{id}"})
    public String save(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("categoryForm") CategoryRequest request, BindingResult errors,
            Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) return page(model, id);
        try {
            service.save(id, request); flash.addFlashAttribute("success", "Đã lưu danh mục."); return "redirect:/categories";
        } catch (DeviceException ex) { errors.reject("category", ex.getMessage()); return page(model, id); }
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try { service.delete(id); flash.addFlashAttribute("success", "Đã xóa danh mục."); }
        catch (DeviceException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/categories";
    }
    private String page(Model model, Long id) {
        model.addAttribute("categories", service.list()); model.addAttribute("categoryId", id); return "categories/list";
    }
}
