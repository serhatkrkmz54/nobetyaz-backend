package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.HolidayCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.HolidayResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.HolidayUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Holiday;
import com.paketnobet.nobetyaz.modules.organization.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {
    private final HolidayRepository holidayRepository;

    @Transactional
    public HolidayResponse create(HolidayCreateRequest request) {
        log.info("Yeni tatil oluşturuluyor: {}", request.name());
        Holiday holiday = Holiday.builder()
                .name(request.name())
                .holidayDate(request.holidayDate())
                .holidayType(request.holidayType())
                .build();
        holidayRepository.save(holiday);
        return toResponse(holiday);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> findAll() {
        return holidayRepository.findAllByOrderByHolidayDateAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HolidayResponse update(UUID id, HolidayUpdateRequest request) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tatil kaydı bulunamadı: " + id));

        holiday.setName(request.name());
        holiday.setHolidayDate(request.holidayDate());
        holiday.setHolidayType(request.holidayType());
        holidayRepository.save(holiday);
        log.info("Tatil kaydı güncellendi: {}", request.name());
        return toResponse(holiday);
    }

    @Transactional
    public void delete(UUID id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tatil kaydı bulunamadı: " + id);
        }
        holidayRepository.deleteById(id);
        log.info("Tatil kaydı silindi: {}", id);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> findHolidays(LocalDate startDate, LocalDate endDate) {
        List<Holiday> holidays;
        if (startDate != null && endDate != null) {
            holidays = holidayRepository.findByHolidayDateBetween(startDate, endDate);
        } else {
            holidays = holidayRepository.findAllByOrderByHolidayDateAsc();
        }
        return holidays.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private HolidayResponse toResponse(Holiday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getName(),
                holiday.getHolidayDate(),
                holiday.getHolidayType()
        );
    }
}
