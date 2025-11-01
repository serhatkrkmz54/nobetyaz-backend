package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record ShiftRequirementResponse(
        UUID id,
        UUID locationId,
        String locationName,
        UUID shiftTemplateId,
        String shiftTemplateName,
        UUID qualificationId,
        String qualificationName,
        int requiredMemberCount
) {
}
