package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id,
                                   String message,
                                   String notificationType,
                                   String status, // "UNREAD" veya "READ"
                                   UUID relatedEntityId,
                                   Instant createdAt) {
}
