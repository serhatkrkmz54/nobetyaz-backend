package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftRequirementRepository extends JpaRepository<ShiftRequirement, UUID> {
    List<ShiftRequirement> findByLocationIdAndShiftTemplateId(UUID locationId, UUID shiftTemplateId);
}
