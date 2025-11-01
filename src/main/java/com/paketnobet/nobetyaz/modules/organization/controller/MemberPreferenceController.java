package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.MemberPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MemberPreferenceController {
    private final MemberPreferenceService preferenceService;

    @GetMapping("/my")
    public ResponseEntity<List<MemberPreferenceResponse>> getMyPreferences(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(preferenceService.findByMember(currentUser));
    }
    @PostMapping
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_MEMBER_PREFERENCES')")
    public ResponseEntity<MemberPreferenceResponse> createPreference(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody MemberPreferenceCreateRequest request) {
        return new ResponseEntity<>(preferenceService.create(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_MEMBER_PREFERENCES')")
    public ResponseEntity<MemberPreferenceResponse> updatePreference(
            @PathVariable UUID id,
            @Valid @RequestBody MemberPreferenceUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(preferenceService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_MEMBER_PREFERENCES')")
    public ResponseEntity<Void> deletePreference(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        preferenceService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

}
