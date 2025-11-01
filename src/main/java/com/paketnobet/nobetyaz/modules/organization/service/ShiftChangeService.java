package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberInfo;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftChangeCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftChangeResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftInfo;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftChangeRequest;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftChangeRequestStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftChangeService {

    private final ShiftChangeRequestRepository changeRequestRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final ScheduleValidatorService scheduleValidator;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ShiftChangeResponse createChangeRequest(ShiftChangeCreateRequest request, UserDetailsImpl currentUser) {
        ScheduledShift initiatingShift = scheduledShiftRepository.findById(request.initiatingShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Sizin nöbetiniz bulunamadı."));
        ScheduledShift targetShift = scheduledShiftRepository.findById(request.targetShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Değişim talep edilen nöbet bulunamadı."));

        if (initiatingShift.getShiftDate().isBefore(LocalDate.now())) {
            throw new RuleViolationException("Geçmiş tarihli bir nöbet için değişim talebi başlatılamaz.");
        }

        // ----- Gerekli Doğrulamalar -----
        if (initiatingShift.getMember() == null || !initiatingShift.getMember().getUser().getId().equals(currentUser.getId())) {
            throw new RuleViolationException("Sadece kendinize atanmış bir nöbet için talep başlatabilirsiniz.");
        }
        if (targetShift.getMember() == null) {
            throw new RuleViolationException("Henüz bir personele atanmamış bir nöbet ile değişim talep edilemez.");
        }
        if (Objects.equals(initiatingShift.getMember().getId(), targetShift.getMember().getId())) {
            throw new RuleViolationException("Bir personel kendi nöbetleri arasında değişim yapamaz.");
        }

        ShiftChangeRequest changeRequest = ShiftChangeRequest.builder()
                .initiatingShift(initiatingShift)
                .initiatingMember(initiatingShift.getMember())
                .targetShift(targetShift)
                .targetMember(targetShift.getMember())
                .status(EShiftChangeRequestStatus.PENDING_TARGET_APPROVAL)
                .requestReason(request.reason())
                .build();

        changeRequestRepository.save(changeRequest);
        notificationService.createAndSendNotification(
                changeRequest.getTargetMember().getUser(),
                currentUser.getUsername() + " size bir nöbet değişimi talebi gönderdi.",
                "SHIFT_CHANGE_REQUEST_NEW",
                changeRequest.getId()
        );
        return toResponse(changeRequest);
    }

    @Transactional
    public ShiftChangeResponse respondByTarget(UUID requestId, boolean accepted, UserDetailsImpl currentUser) {
        ShiftChangeRequest request = findRequestByIdOrThrow(requestId);

        if (request.getTargetMember().getUser() == null || !request.getTargetMember().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bu talebe sadece hedef personel yanıt verebilir.");
        }
        if (request.getStatus() != EShiftChangeRequestStatus.PENDING_TARGET_APPROVAL) {
            throw new RuleViolationException("Bu talep artık yanıtlanamaz durumdadır.");
        }

        if (accepted) {
            request.setStatus(EShiftChangeRequestStatus.PENDING_MANAGER_APPROVAL);

            // --- BİLDİRİM ENTEGRASYONU (KABUL) ---
            notificationService.createAndSendNotification(
                    request.getInitiatingMember().getUser(),
                    currentUser.getUsername() + ", nöbet değişimi talebinizi kabul etti. Yönetici onayı bekleniyor.",
                    "SHIFT_CHANGE_REQUEST_ACCEPTED",
                    request.getId()
            );

            Set<User> managers = new HashSet<>(userRepository.findByRoles_Name(ERole.ROLE_ADMIN));
            managers.addAll(userRepository.findByRoles_Name(ERole.ROLE_SCHEDULER));
            managers.forEach(manager ->
                    notificationService.createAndSendNotification(
                            manager,
                            "Onayınızı bekleyen yeni bir nöbet değişim talebi var.",
                            "SHIFT_CHANGE_REQUEST_FOR_APPROVAL",
                            request.getId()
                    )
            );
        } else {
            request.setStatus(EShiftChangeRequestStatus.REJECTED);

            notificationService.createAndSendNotification(
                    request.getInitiatingMember().getUser(),
                    currentUser.getUsername() + ", nöbet değişimi talebinizi reddetti.",
                    "SHIFT_CHANGE_REQUEST_REJECTED",
                    request.getId()
            );
        }

        changeRequestRepository.save(request);
        return toResponse(request);
    }

    @Transactional
    @Auditable(actionType = "APPROVE_SHIFT_CHANGE")
    public ShiftChangeResponse resolveByManager(UUID requestId, boolean approved, String notes) {
        ShiftChangeRequest request = findRequestByIdOrThrow(requestId);

        if (request.getStatus() != EShiftChangeRequestStatus.PENDING_MANAGER_APPROVAL) {
            throw new RuleViolationException("Bu talep yönetici onayı aşamasında değildir.");
        }

        if (approved) {
            // Değişim öncesi her iki personel için de yeni nöbetlerin kurallara uygunluğunu tekrar kontrol et
            scheduleValidator.validateAssignment(request.getInitiatingMember(), request.getTargetShift());
            scheduleValidator.validateAssignment(request.getTargetMember(), request.getInitiatingShift());

            // NÖBETLERİ FİİLEN DEĞİŞTİR (SWAP)
            Member initiatingMember = request.getInitiatingMember();
            Member targetMember = request.getTargetMember();

            request.getInitiatingShift().setMember(targetMember);
            request.getTargetShift().setMember(initiatingMember);

            scheduledShiftRepository.save(request.getInitiatingShift());
            scheduledShiftRepository.save(request.getTargetShift());

            request.setStatus(EShiftChangeRequestStatus.APPROVED);
        } else {
            request.setStatus(EShiftChangeRequestStatus.REJECTED);
        }

        String message = approved ? "Nöbet değişim talebiniz yönetici tarafından ONAYLANDI." : "Nöbet değişim talebiniz yönetici tarafından REDDEDİLDİ.";
        String type = approved ? "SHIFT_CHANGE_APPROVED" : "SHIFT_CHANGE_REJECTED_BY_MANAGER";
        notificationService.createAndSendNotification(request.getInitiatingMember().getUser(), message, type, request.getId());
        notificationService.createAndSendNotification(request.getTargetMember().getUser(), message, type, request.getId());

        request.setResolutionNotes(notes);
        changeRequestRepository.save(request);
        return toResponse(request);
    }

    @Transactional
    public ShiftChangeResponse cancelRequest(UUID requestId, UserDetailsImpl currentUser) {
        ShiftChangeRequest request = findRequestByIdOrThrow(requestId);

        if (request.getInitiatingMember().getUser() == null || !request.getInitiatingMember().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bu talebi sadece başlatan kişi iptal edebilir.");
        }
        if (request.getStatus() != EShiftChangeRequestStatus.PENDING_TARGET_APPROVAL && request.getStatus() != EShiftChangeRequestStatus.PENDING_MANAGER_APPROVAL) {
            throw new RuleViolationException("Onaylanmış veya reddedilmiş bir talep iptal edilemez.");
        }

        request.setStatus(EShiftChangeRequestStatus.CANCELLED);
        changeRequestRepository.save(request);
        notificationService.createAndSendNotification(
                request.getTargetMember().getUser(),
                currentUser.getUsername() + " nöbet değişimi talebini iptal etti.",
                "SHIFT_CHANGE_CANCELLED",
                request.getId()
        );
        return toResponse(request);
    }

    public List<ShiftChangeResponse> findMyRequests(UserDetailsImpl currentUser) {
        return memberRepository.findByUserId(currentUser.getId())
                .map(member -> {
                    UUID memberId = member.getId();
                    return changeRequestRepository.findByInitiatingMemberIdOrTargetMemberId(memberId, memberId)
                            .stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    private ShiftChangeRequest findRequestByIdOrThrow(UUID requestId) {
        return changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Değişim talebi bulunamadı: " + requestId));
    }

    private ShiftChangeResponse toResponse(ShiftChangeRequest request) {
        ShiftInfo initiatingShiftInfo = new ShiftInfo(
                request.getInitiatingShift().getId(),
                request.getInitiatingShift().getShiftDate(),
                request.getInitiatingShift().getShiftTemplate().getStartTime(),
                request.getInitiatingShift().getShiftTemplate().getEndTime(),
                request.getInitiatingShift().getLocation().getName()
        );
        MemberInfo initiatingMemberInfo = new MemberInfo(
                request.getInitiatingMember().getId(),
                request.getInitiatingMember().getFirstName(),
                request.getInitiatingMember().getLastName()
        );
        ShiftInfo targetShiftInfo = new ShiftInfo(
                request.getTargetShift().getId(),
                request.getTargetShift().getShiftDate(),
                request.getTargetShift().getShiftTemplate().getStartTime(),
                request.getTargetShift().getShiftTemplate().getEndTime(),
                request.getTargetShift().getLocation().getName()
        );
        MemberInfo targetMemberInfo = new MemberInfo(
                request.getTargetMember().getId(),
                request.getTargetMember().getFirstName(),
                request.getTargetMember().getLastName()
        );

        return new ShiftChangeResponse(
                request.getId(),
                initiatingShiftInfo,
                initiatingMemberInfo,
                targetShiftInfo,
                targetMemberInfo,
                request.getStatus().name(),
                request.getRequestReason(),
                request.getResolutionNotes()
        );
    }

}
