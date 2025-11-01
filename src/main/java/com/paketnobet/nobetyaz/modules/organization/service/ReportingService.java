package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.AdminDashboardStatsResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberMonthlySummaryResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.LeaveRecord;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftBidStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftChangeRequestStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final MemberRepository memberRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final LeaveRecordRepository leaveRecordRepository;
    private final ShiftChangeRequestRepository shiftChangeRequestRepository;
    private final ShiftBidRepository shiftBidRepository;

    public MemberMonthlySummaryResponse generateMonthlySummary(UUID memberId, int year, int month) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        List<ScheduledShift> shifts = scheduledShiftRepository.findByMemberIdAndShiftDateBetween(memberId, startDate, endDate);
        double totalHours = shifts.stream()
                .mapToDouble(shift -> shift.getShiftTemplate().getDurationInHours())
                .sum();

        List<LeaveRecord> leaves = leaveRecordRepository.findApprovedLeavesForMemberInPeriod(memberId, startDate, endDate);
        long totalDaysOnLeave = 0;
        for (LeaveRecord leave : leaves) {
            LocalDate effectiveStartDate = leave.getStartDate().isBefore(startDate) ? startDate : leave.getStartDate();
            LocalDate effectiveEndDate = leave.getEndDate().isAfter(endDate) ? endDate : leave.getEndDate();

            totalDaysOnLeave += ChronoUnit.DAYS.between(effectiveStartDate, effectiveEndDate) + 1;
        }

        return new MemberMonthlySummaryResponse(
                memberId,
                member.getFirstName() + " " + member.getLastName(),
                year,
                month,
                totalHours,
                totalDaysOnLeave
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminDashboardStats(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        long totalMembers = memberRepository.countByIsActive(true);

        long openShifts = scheduledShiftRepository.countByStatusAndShiftDateBetween(EShiftStatus.OPEN, startDate, endDate);
        long biddingShifts = scheduledShiftRepository.countByStatusAndShiftDateBetween(EShiftStatus.BIDDING, startDate, endDate);
        long confirmedShifts = scheduledShiftRepository.countByStatusAndShiftDateBetween(EShiftStatus.CONFIRMED, startDate, endDate);

        long pendingLeaves = leaveRecordRepository.countByStatus(ELeaveStatus.PENDING);
        long pendingChanges = shiftChangeRequestRepository.countByStatus(EShiftChangeRequestStatus.PENDING_MANAGER_APPROVAL);
        long pendingBids = shiftBidRepository.countByBidStatus(EShiftBidStatus.ACTIVE);

        return new AdminDashboardStatsResponse(
                totalMembers,
                openShifts,
                biddingShifts,
                confirmedShifts,
                pendingChanges,
                pendingLeaves,
                pendingBids,
                startDate,
                endDate
        );
    }

}
