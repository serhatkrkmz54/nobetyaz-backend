package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.HolidayCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.HolidayResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.HolidayUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/management/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<HolidayResponse> createHoliday(@Valid @RequestBody HolidayCreateRequest request) {
        return new ResponseEntity<>(holidayService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<HolidayResponse> holidays = holidayService.findHolidays(startDate, endDate);
        return ResponseEntity.ok(holidays);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<HolidayResponse> updateHoliday(
            @PathVariable UUID id,
            @Valid @RequestBody HolidayUpdateRequest request) {
        return ResponseEntity.ok(holidayService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        holidayService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
