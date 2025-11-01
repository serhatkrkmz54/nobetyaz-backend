package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.*;
import com.paketnobet.nobetyaz.modules.organization.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests/leave")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestResponse>> getMyLeaveRequests(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(leaveRequestService.getMyLeaveRequests(currentUser));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return new ResponseEntity<>(leaveRequestService.createLeaveRequest(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancelLeaveRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(leaveRequestService.cancelLeaveRequest(id, currentUser));
    }

    // --- Yönetici (Admin/Scheduler) Endpoint'leri ---
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<LeaveRequestResponse>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<LeaveRequestResponse> approveLeaveRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveRequestService.approveLeaveRequest(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveRequestService.rejectLeaveRequest(id));
    }

    // --- Ortak (Takvim) Endpoint'i ---
    @GetMapping("/approved-by-period")
    public ResponseEntity<List<LeaveRequestResponse>> getApprovedLeavesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(leaveRequestService.getApprovedLeavesByPeriod(startDate, endDate));
    }
}
