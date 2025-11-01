package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftTemplateUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftTemplate;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;

    @Transactional
    public ShiftTemplateResponse create(ShiftTemplateCreateRequest request) {
        ShiftTemplate template = ShiftTemplate.builder()
                .name(request.name())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isNightShift(request.isNightShift())
                .durationInHours(calculateDuration(request.startTime(), request.endTime()))
                .build();
        shiftTemplateRepository.save(template);
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<ShiftTemplateResponse> findAll(boolean includeInactive) {
        List<ShiftTemplate> templates;
        if (includeInactive) {
            templates = shiftTemplateRepository.findAll();
        } else {
            templates = shiftTemplateRepository.findAllByIsActive(true);
        }
        return templates.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShiftTemplateResponse findById(UUID id) {
        ShiftTemplate template = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftTemplate not found with id: " + id));
        return toResponse(template);
    }

    @Transactional
    public ShiftTemplateResponse update(UUID id, ShiftTemplateUpdateRequest request) {
        ShiftTemplate template = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftTemplate not found with id: " + id));

        template.setName(request.name());
        template.setStartTime(request.startTime());
        template.setEndTime(request.endTime());
        template.setNightShift(request.isNightShift());
        template.setActive(request.isActive());
        template.setDurationInHours(calculateDuration(request.startTime(), request.endTime()));

        shiftTemplateRepository.save(template);
        return toResponse(template);
    }

    @Transactional
    public void delete(UUID id) {
        if (!shiftTemplateRepository.existsById(id)) {
            throw new ResourceNotFoundException("ShiftTemplate not found with id: " + id);
        }
        shiftTemplateRepository.deleteById(id);
    }

    private double calculateDuration(LocalTime start, LocalTime end) {
        Duration duration;
        if (end.isBefore(start)) {
            duration = Duration.between(start, LocalTime.MAX).plus(Duration.between(LocalTime.MIN, end)).plusNanos(1);
        } else {
            duration = Duration.between(start, end);
        }
        return duration.toMinutes() / 60.0;
    }

    private ShiftTemplateResponse toResponse(ShiftTemplate template) {
        return new ShiftTemplateResponse(
                template.getId(), template.getName(), template.getStartTime(), template.getEndTime(),
                template.getDurationInHours(), template.isNightShift(),template.isActive()
        );
    }
}