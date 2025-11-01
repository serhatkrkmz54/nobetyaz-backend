package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftRequirementCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftRequirementResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Location;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Qualification;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftRequirement;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftTemplate;
import com.paketnobet.nobetyaz.modules.organization.repository.LocationRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.QualificationRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftRequirementRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftRequirementService {

    private final ShiftRequirementRepository shiftRequirementRepository;
    private final LocationRepository locationRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final QualificationRepository qualificationRepository;

    @Transactional
    public ShiftRequirementResponse create(ShiftRequirementCreateRequest request) {
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        ShiftTemplate shiftTemplate = shiftTemplateRepository.findById(request.shiftTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift Template not found"));

        Qualification qualification = null;
        if (request.qualificationId() != null) {
            qualification = qualificationRepository.findById(request.qualificationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Qualification not found"));
        }

        ShiftRequirement requirement = ShiftRequirement.builder()
                .location(location)
                .shiftTemplate(shiftTemplate)
                .qualification(qualification)
                .requiredMemberCount(request.requiredMemberCount())
                .build();

        shiftRequirementRepository.save(requirement);

        return toResponse(requirement);
    }

    public List<ShiftRequirementResponse> findByLocationAndShiftTemplate(UUID locationId, UUID shiftTemplateId) {
        return shiftRequirementRepository.findByLocationIdAndShiftTemplateId(locationId, shiftTemplateId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        if (!shiftRequirementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shift Requirement not found with id: " + id);
        }
        shiftRequirementRepository.deleteById(id);
    }

    private ShiftRequirementResponse toResponse(ShiftRequirement requirement) {
        String qualificationName = (requirement.getQualification() != null)
                ? requirement.getQualification().getName()
                : "Herhangi Bir Personel";
        UUID qualificationId = (requirement.getQualification() != null)
                ? requirement.getQualification().getId()
                : null;

        return new ShiftRequirementResponse(
                requirement.getId(),
                requirement.getLocation().getId(),
                requirement.getLocation().getName(),
                requirement.getShiftTemplate().getId(),
                requirement.getShiftTemplate().getName(),
                qualificationId,
                qualificationName,
                requirement.getRequiredMemberCount()
        );
    }
}
