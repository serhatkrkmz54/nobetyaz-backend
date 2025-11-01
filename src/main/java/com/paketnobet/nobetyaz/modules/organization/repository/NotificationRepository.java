package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Notification;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ENotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientUserId);
    List<Notification> findByRecipientIdAndStatusInOrderByCreatedAtDesc(UUID recipientId, List<ENotificationStatus> statuses);
    Optional<Notification> findByIdAndRecipientId(UUID notificationId, UUID recipientId);
}
