package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.ShiftRequirementCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftRequirementResponse;
import com.paketnobet.nobetyaz.modules.organization.service.ShiftRequirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shift-requirements")
@RequiredArgsConstructor
public class ShiftRequirementController {

    private final ShiftRequirementService shiftRequirementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ShiftRequirementResponse> createShiftRequirement(@Valid @RequestBody ShiftRequirementCreateRequest request) {
        ShiftRequirementResponse response = shiftRequirementService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<ShiftRequirementResponse>> getShiftRequirements(
            @RequestParam UUID locationId,
            @RequestParam UUID shiftTemplateId) {
        List<ShiftRequirementResponse> responses = shiftRequirementService.findByLocationAndShiftTemplate(locationId, shiftTemplateId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Void> deleteShiftRequirement(@PathVariable UUID id) {
        shiftRequirementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
