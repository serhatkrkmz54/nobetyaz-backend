package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.organization.dto.LeaveRecordCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.LeaveRequestCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.LeaveRequestResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.LeaveRecord;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.LeaveRecordRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestService {

    private final LeaveRecordRepository leaveRecordRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ScheduledShiftRepository scheduledShiftRepository;

    private Member getMemberFromUser(UserDetailsImpl currentUser) {
        return memberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bu kullanıcıya ait personel kaydı bulunamadı. ID: " + currentUser.getId()
                ));
    }

    private Set<User> getAdminsAndSchedulers() {
        List<User> admins = userRepository.findByRoles_Name(ERole.ROLE_ADMIN);
        List<User> schedulers = userRepository.findByRoles_Name(ERole.ROLE_SCHEDULER);
        return Stream.concat(admins.stream(), schedulers.stream())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getMyLeaveRequests(UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        return leaveRecordRepository.findByMemberIdWithMemberOrderByStartDateDesc(member.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAllLeaveRequests() {
        return leaveRecordRepository.findAllWithMemberOrderByStartDateDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getApprovedLeavesByPeriod(LocalDate startDate, LocalDate endDate) {
        List<LeaveRecord> approvedLeaves = leaveRecordRepository.findApprovedLeavesByDateRangeWithMember(
                startDate,
                endDate,
                ELeaveStatus.APPROVED
        );
        return approvedLeaves.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponse createLeaveRequest(LeaveRequestCreateRequest request, UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        if(request.startDate().isAfter(request.endDate())) {
            throw new RuleViolationException("Başlangıç tarihi, bitiş tarihinden sonra olamaz.");
        }

        checkForConflictingShifts(member, request.startDate(), request.endDate());

        LeaveRecord leaveRecord = LeaveRecord.builder()
                .member(member)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reason(request.reason())
                .status(ELeaveStatus.PENDING)
                .build();
        leaveRecordRepository.save(leaveRecord);

        String message = member.getFirstName() + " " + member.getLastName() + " yeni bir izin talebinde bulundu.";
        getAdminsAndSchedulers().forEach(manager ->
                notificationService.createAndSendNotification(
                        manager, message, "LEAVE_REQUEST_NEW", leaveRecord.getId())
        );
        return toResponse(leaveRecord);
    }

    @Transactional
    public LeaveRequestResponse cancelLeaveRequest(UUID leaveId, UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        LeaveRecord leaveRecord = leaveRecordRepository.findByIdAndMemberId(leaveId, member.getId())
                .orElseThrow(() -> new ResourceNotFoundException("İzin talebi bulunamadı veya size ait değil."));

        if (leaveRecord.getStatus() != ELeaveStatus.PENDING && leaveRecord.getStatus() != ELeaveStatus.APPROVED) {
            throw new RuleViolationException("Sadece 'Onay Bekleyen' veya 'Onaylanmış' izinler iptal edilebilir.");
        }

        ELeaveStatus oldStatus = leaveRecord.getStatus();
        leaveRecord.setStatus(ELeaveStatus.CANCELLED);
        leaveRecordRepository.save(leaveRecord);

        if (oldStatus == ELeaveStatus.APPROVED) {
            String message = leaveRecord.getMember().getFirstName() + " " + leaveRecord.getMember().getLastName() + ", onaylanmış bir iznini iptal etti.";
            getAdminsAndSchedulers().forEach(manager ->
                    notificationService.createAndSendNotification(
                            manager, message, "LEAVE_REQUEST_CANCELLED", leaveRecord.getId())
            );
        }
        return toResponse(leaveRecord);
    }

    @Transactional
    @Auditable(actionType = "APPROVE_LEAVE_REQUEST")
    public LeaveRequestResponse approveLeaveRequest(UUID leaveId) {
        LeaveRecord leaveRecord = leaveRecordRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("İzin talebi bulunamadı: " + leaveId));
        if (leaveRecord.getStatus() != ELeaveStatus.PENDING) {
            throw new RuleViolationException("Sadece 'Onay Bekleyen' talepler onaylanabilir.");
        }
        checkForConflictingShifts(leaveRecord.getMember(), leaveRecord.getStartDate(), leaveRecord.getEndDate());
        leaveRecord.setStatus(ELeaveStatus.APPROVED);
        leaveRecordRepository.save(leaveRecord);

        notificationService.createAndSendNotification(
                leaveRecord.getMember().getUser(),
                "İzin talebiniz (Başlangıç: " + leaveRecord.getStartDate() + ") ONAYLANDI.",
                "LEAVE_APPROVED", leaveId);
        return toResponse(leaveRecord);
    }

    @Transactional
    @Auditable(actionType = "REJECT_LEAVE_REQUEST")
    public LeaveRequestResponse rejectLeaveRequest(UUID leaveId) {
        LeaveRecord leaveRecord = leaveRecordRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("İzin talebi bulunamadı: " + leaveId));
        if (leaveRecord.getStatus() != ELeaveStatus.PENDING) {
            throw new RuleViolationException("Sadece 'Onay Bekleyen' talepler reddedilebilir.");
        }
        leaveRecord.setStatus(ELeaveStatus.REJECTED);
        leaveRecordRepository.save(leaveRecord);

        notificationService.createAndSendNotification(
                leaveRecord.getMember().getUser(),
                "İzin talebiniz (Başlangıç: " + leaveRecord.getStartDate() + ") REDDEDİLDİ.",
                "LEAVE_REJECTED", leaveId);
        return toResponse(leaveRecord);
    }

    private void checkForConflictingShifts(Member member, LocalDate startDate, LocalDate endDate) {
        log.debug("{} personeli için {} - {} arası çakışan nöbet kontrolü yapılıyor...", member.getFirstName(), startDate, endDate);

        List<ScheduledShift> conflictingShifts = scheduledShiftRepository
                .findByMemberIdAndShiftDateBetweenAndStatusNotIn(
                        member.getId(),
                        startDate,
                        endDate,
                        Set.of(EShiftStatus.OPEN, EShiftStatus.BIDDING)
                );

        if (!conflictingShifts.isEmpty()) {
            log.warn("Çakışma bulundu! {} personeli, izin istenen tarih aralığında {} nöbetine sahip.",
                    member.getFirstName(), conflictingShifts.getFirst().getShiftDate());

            throw new RuleViolationException(
                    "İzin talebi oluşturulamaz/onaylanamaz. Personelin bu tarih aralığında zaten atanmış bir nöbeti bulunmaktadır."
            );
        }
    }

    private LeaveRequestResponse toResponse(LeaveRecord record) {
        Member member = record.getMember();

        LeaveRequestResponse.MemberInfo memberInfo = new LeaveRequestResponse.MemberInfo(
                member.getId(), member.getFirstName(), member.getLastName(),
                member.getEmployeeId(), member.getPhoneNumber());

        return new LeaveRequestResponse(
                record.getId(), memberInfo, record.getLeaveType(),
                record.getStartDate(), record.getEndDate(), record.getReason(),
                record.getStatus(), record.getCreatedAt());
    }
}