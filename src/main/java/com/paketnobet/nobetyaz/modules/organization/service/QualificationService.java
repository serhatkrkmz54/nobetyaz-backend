package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Qualification;
import com.paketnobet.nobetyaz.modules.organization.repository.QualificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationService {

    private final QualificationRepository qualificationRepository;

    @Transactional
    public QualificationResponse create(QualificationCreateRequest request) {
        Qualification qualification = Qualification.builder()
                .name(request.name())
                .description(request.description())
                .build();
        qualificationRepository.save(qualification);
        return toResponse(qualification);
    }

    public List<QualificationResponse> findAll() {
        return qualificationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QualificationResponse findById(UUID id) {
        Qualification qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with id: " + id));
        return toResponse(qualification);
    }

    @Transactional
    public QualificationResponse update(UUID id, QualificationUpdateRequest request) {
        Qualification qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with id: " + id));
        qualification.setName(request.name());
        qualification.setDescription(request.description());
        qualificationRepository.save(qualification);

        return toResponse(qualification);
    }

    public void delete(UUID id) {
        if (!qualificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualification not found with id: " + id);
        }
        qualificationRepository.deleteById(id);
    }

    private QualificationResponse toResponse(Qualification qualification) {
        return new QualificationResponse(
                qualification.getId(),
                qualification.getName(),
                qualification.getDescription()
        );
    }

}
