package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.QualificationCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.QualificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<QualificationResponse> createQualification(@Valid @RequestBody QualificationCreateRequest request) {
        return new ResponseEntity<>(qualificationService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<QualificationResponse>> getAllQualifications() {
        return ResponseEntity.ok(qualificationService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<QualificationResponse> getQualificationById(@PathVariable UUID id) {
        return ResponseEntity.ok(qualificationService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<QualificationResponse> updateQualification(@PathVariable UUID id, @Valid @RequestBody QualificationUpdateRequest request) {
        return ResponseEntity.ok(qualificationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Void> deleteQualification(@PathVariable UUID id) {
        qualificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
