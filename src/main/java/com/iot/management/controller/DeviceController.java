package com.iot.management.controller;

import com.iot.management.dto.*;
import com.iot.management.entity.DeviceStatus;
import com.iot.management.security.CustomUserDetails;
import com.iot.management.service.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;
    public DeviceController(DeviceService service) { this.service = service; }
    @GetMapping
    public Page<DeviceResponse> list(@AuthenticationPrincipal CustomUserDetails actor,
            @RequestParam(defaultValue = "") String q, @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) DeviceStatus status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) { return service.list(actor, q, categoryId, status, page, size); }
    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor) { return service.get(id, actor); }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request,
            @AuthenticationPrincipal CustomUserDetails actor) { return created(service.create(request, null, actor)); }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeviceResponse> createWithImage(@Valid @ModelAttribute DeviceRequest request,
            @RequestParam(required = false) MultipartFile image, @AuthenticationPrincipal CustomUserDetails actor) {
        return created(service.create(request, image, actor));
    }
    @PutMapping("/{id}")
    public DeviceResponse update(@PathVariable Long id, @Valid @RequestBody DeviceRequest request,
            @AuthenticationPrincipal CustomUserDetails actor) { return service.update(id, request, null, actor); }
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeviceResponse image(@PathVariable Long id, @RequestParam MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails actor) { return service.uploadImage(id, image, actor); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor) { service.delete(id, actor); }
    private ResponseEntity<DeviceResponse> created(DeviceResponse device) {
        return ResponseEntity.created(URI.create("/api/devices/" + device.id())).body(device);
    }
}
