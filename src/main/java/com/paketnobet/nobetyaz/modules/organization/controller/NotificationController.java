package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.NotificationResponse;
import com.paketnobet.nobetyaz.modules.organization.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/active")
    public ResponseEntity<List<NotificationResponse>> getActiveNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(notificationService.findActiveByRecipient(currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<NotificationResponse> notifications = notificationService.findAllByRecipient(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/archive")
    public ResponseEntity<NotificationResponse> archiveNotification(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        NotificationResponse response = notificationService.archiveNotification(notificationId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/mark-as-read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        NotificationResponse response = notificationService.markAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
