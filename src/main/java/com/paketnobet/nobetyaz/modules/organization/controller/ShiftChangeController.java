package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftChangeActionRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftChangeCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftChangeResponse;
import com.paketnobet.nobetyaz.modules.organization.service.ShiftChangeService;
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
@RequestMapping("/api/shift-changes")
@RequiredArgsConstructor
public class ShiftChangeController {

    private final ShiftChangeService shiftChangeService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShiftChangeResponse> createChangeRequest(
            @Valid @RequestBody ShiftChangeCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        ShiftChangeResponse response = shiftChangeService.createChangeRequest(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Giriş yapmış kullanıcının dahil olduğu (başlatan veya hedef olan) tüm talepleri listeler.
     */
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShiftChangeResponse>> getMyRequests(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<ShiftChangeResponse> requests = shiftChangeService.findMyRequests(currentUser);
        return ResponseEntity.ok(requests);
    }

    /**
     * Hedef personelin talebe yanıt vermesi (kabul veya red).
     */
    @PutMapping("/{requestId}/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShiftChangeResponse> respondToRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ShiftChangeActionRequest action,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        boolean accepted = action.action().equalsIgnoreCase("ACCEPT");
        ShiftChangeResponse response = shiftChangeService.respondByTarget(requestId, accepted, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Yöneticinin talebi sonuca bağlaması (onay veya red).
     */
    @PutMapping("/{requestId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ShiftChangeResponse> resolveRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ShiftChangeActionRequest action) {
        boolean approved = action.action().equalsIgnoreCase("APPROVE");
        ShiftChangeResponse response = shiftChangeService.resolveByManager(requestId, approved, action.notes());
        return ResponseEntity.ok(response);
    }

    /**
     * Talebi başlatan kişinin talebini iptal etmesi.
     */
    @PutMapping("/{requestId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShiftChangeResponse> cancelRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        ShiftChangeResponse response = shiftChangeService.cancelRequest(requestId, currentUser);
        return ResponseEntity.ok(response);
    }

}
