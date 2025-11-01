package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.AdminDashboardStatsResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberMonthlySummaryResponse;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;
    private final MemberRepository memberRepository;

    @GetMapping("/admin-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<AdminDashboardStatsResponse> getAdminDashboardStats() {
        YearMonth currentMonth = YearMonth.now();
        return ResponseEntity.ok(reportingService.getAdminDashboardStats(currentMonth));
    }

    @GetMapping("/members/{memberId}/monthly-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER') or @securityService.isOwnerOfMember(#memberId, principal)")
    public ResponseEntity<MemberMonthlySummaryResponse> getMemberMonthlySummary(
            @PathVariable UUID memberId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        MemberMonthlySummaryResponse summary = reportingService.generateMonthlySummary(memberId, year, month);
        return ResponseEntity.ok(summary);
    }

}
