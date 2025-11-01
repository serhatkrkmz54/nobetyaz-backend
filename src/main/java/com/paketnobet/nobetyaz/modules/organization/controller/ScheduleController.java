package com.paketnobet.nobetyaz.modules.organization.controller;

import ai.timefold.solver.core.api.solver.SolverStatus;
import com.paketnobet.nobetyaz.modules.organization.dto.AssignMemberRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ScheduleGenerateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ScheduledShiftResponse;
import com.paketnobet.nobetyaz.modules.organization.service.AutoSchedulerService;
import com.paketnobet.nobetyaz.modules.organization.service.ScheduleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final AutoSchedulerService autoSchedulerService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<String> generateSchedule(@Valid @RequestBody ScheduleGenerateRequest request) {
        scheduleService.generateScheduleForMonth(request.year(), request.month());
        return ResponseEntity.ok(request.year() + "/" + request.month() + " için çizelge başarıyla oluşturuldu.");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduledShiftResponse>> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScheduledShiftResponse> schedule = scheduleService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{shiftId:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledShiftResponse> getScheduleById(@PathVariable UUID shiftId) {
        return ResponseEntity.ok(scheduleService.findShiftById(shiftId));
    }

    @PutMapping("/{shiftId:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<ScheduledShiftResponse> assignMemberToShift(
            @PathVariable UUID shiftId,
            @Valid @RequestBody AssignMemberRequest request) {
        ScheduledShiftResponse updatedShift = scheduleService.assignMember(shiftId, request.memberId());
        return ResponseEntity.ok(updatedShift);
    }

    @PostMapping("/solve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<UUID> solveSchedule(
            @RequestParam int year,
            @RequestParam int month) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        UUID problemId = autoSchedulerService.solve(startDate, endDate);

        return new ResponseEntity<>(problemId, HttpStatus.ACCEPTED);
    }

    @GetMapping("/solve/status/{problemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<SolverStatus> getSolverStatus(@PathVariable UUID problemId) {
        SolverStatus status = autoSchedulerService.getSolverStatus(problemId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Resource> exportSchedule(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletResponse response) {

        String filename = String.format("Cizelge_%d_%02d.xlsx", year, month);
        ByteArrayInputStream bis = scheduleService.exportScheduleToExcel(year, month);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

}
