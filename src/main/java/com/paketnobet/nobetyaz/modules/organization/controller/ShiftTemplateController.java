package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.ShiftTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift-templates")
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ShiftTemplateResponse> createShiftTemplate(@Valid @RequestBody ShiftTemplateCreateRequest request) {
        return new ResponseEntity<>(shiftTemplateService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShiftTemplateResponse>> getAllShiftTemplates(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(shiftTemplateService.findAll(includeInactive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ShiftTemplateResponse> getShiftTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftTemplateService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ShiftTemplateResponse> updateShiftTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody ShiftTemplateUpdateRequest request) {
        return ResponseEntity.ok(shiftTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Void> deleteShiftTemplate(@PathVariable UUID id) {
        shiftTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
