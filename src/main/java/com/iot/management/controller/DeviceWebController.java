package com.iot.management.controller;

import com.iot.management.dto.*;
import com.iot.management.entity.DeviceStatus;
import com.iot.management.exception.DeviceException;
import com.iot.management.security.CustomUserDetails;
import com.iot.management.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;

@Controller
@RequestMapping("/devices")
public class DeviceWebController {
    private final DeviceService devices;
    private final CategoryService categories;
    private final CloudinaryService images;
    public DeviceWebController(DeviceService devices, CategoryService categories, CloudinaryService images) {
        this.devices = devices; this.categories = categories; this.images = images;
    }
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails actor, @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long categoryId, @RequestParam(required = false) DeviceStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        org.springframework.data.domain.Page<DeviceResponse> result = devices.list(actor, q, categoryId, status, page, size);
        if (page > 0 && result.isEmpty()) {
            result = devices.list(actor, q, categoryId, status, Math.max(0, result.getTotalPages() - 1), size);
        }
        model.addAttribute("devices", result);
        model.addAttribute("categories", categories.list()); model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("q", q); model.addAttribute("categoryId", categoryId); model.addAttribute("status", status);
        model.addAttribute("size", size);
        return "devices/list";
    }
    @GetMapping("/new")
    public String createForm(Model model) { model.addAttribute("deviceForm", new DeviceRequest()); return form(model, null, null); }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor, Model model) {
        model.addAttribute("device", devices.get(id, actor)); return "devices/detail";
    }
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor, Model model) {
        DeviceResponse d = devices.get(id, actor);
        DeviceRequest request = new DeviceRequest(); request.setName(d.name()); request.setSerialNumber(d.serialNumber());
        request.setDescription(d.description()); request.setLocation(d.location()); request.setStatus(d.status()); request.setCategoryId(d.categoryId());
        model.addAttribute("deviceForm", request); return form(model, id, d.imageUrl());
    }
    @PostMapping
    public String create(@Valid @ModelAttribute("deviceForm") DeviceRequest request, BindingResult errors,
            @RequestParam(required = false) MultipartFile image, @AuthenticationPrincipal CustomUserDetails actor,
            Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) return form(model, null, null);
        try {
            DeviceResponse d = devices.create(request, image, actor);
            flash.addFlashAttribute("success", "Đã thêm thiết bị thành công."); return "redirect:/devices/" + d.id();
        } catch (DeviceException ex) { errors.reject("device", ex.getMessage()); return form(model, null, null); }
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("deviceForm") DeviceRequest request,
            BindingResult errors, @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails actor, Model model, RedirectAttributes flash) {
        DeviceResponse current = devices.get(id, actor);
        if (errors.hasErrors()) return form(model, id, current.imageUrl());
        try {
            devices.update(id, request, image, actor);
            flash.addFlashAttribute("success", "Đã cập nhật thiết bị."); return "redirect:/devices/" + id;
        } catch (DeviceException ex) { errors.reject("device", ex.getMessage()); return form(model, id, current.imageUrl()); }
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor, RedirectAttributes flash) {
        devices.delete(id, actor); flash.addFlashAttribute("success", "Đã xóa thiết bị."); return "redirect:/devices";
    }
    private String form(Model model, Long id, String imageUrl) {
        model.addAttribute("deviceId", id); model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("categories", categories.list()); model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("cloudinaryConfigured", images.isConfigured()); return "devices/form";
    }
}
