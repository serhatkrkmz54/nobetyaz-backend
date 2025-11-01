package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.modules.organization.dto.NotificationResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Notification;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ENotificationStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createAndSendNotification(User recipient, String message, String type, UUID relatedEntityId) {
        try {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .message(message)
                    .notificationType(type)
                    .relatedEntityId(relatedEntityId)
                    .status(ENotificationStatus.UNREAD)
                    .build();
            notificationRepository.save(notification);

            String userChannel = "/user/" + recipient.getUsername() + "/queue/notifications";
            messagingTemplate.convertAndSend(userChannel, notification);
            log.info("Bildirim gönderildi: Kullanıcı={}, Tip={}", recipient.getUsername(), type);

        } catch (Exception e) {
            log.error("Bildirim oluşturma veya gönderme hatası: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAllByRecipient(UUID recipientUserId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findActiveByRecipient(UUID recipientUserId) {
        return notificationRepository.findByRecipientIdAndStatusInOrderByCreatedAtDesc(
                        recipientUserId,
                        List.of(ENotificationStatus.UNREAD, ENotificationStatus.READ) // Sadece bu ikisini getir
                ).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı veya size ait değil."));

        if (notification.getStatus() == ENotificationStatus.UNREAD) {
            notification.setStatus(ENotificationStatus.READ);
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse archiveNotification(UUID notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı veya size ait değil."));

        if (notification.getStatus() == ENotificationStatus.READ) {
            notification.setStatus(ENotificationStatus.ARCHIVED);
            notificationRepository.save(notification);
            log.info("Bildirim (ID: {}) 'ARCHIVED' olarak işaretlendi.", notificationId);
        } else {
            log.warn("Bildirim (ID: {}) 'READ' durumunda olmadığı için arşivlenemedi.", notificationId);
        }
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getStatus().name(),
                notification.getRelatedEntityId(),
                notification.getCreatedAt()
        );
    }
}
