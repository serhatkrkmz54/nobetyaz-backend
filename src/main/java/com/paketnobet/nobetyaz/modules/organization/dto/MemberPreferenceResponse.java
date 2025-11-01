package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record MemberPreferenceResponse(
        UUID id,
        UUID memberId,
        UUID shiftTemplateId,
        String shiftTemplateName,
        String shiftTemplateTime,
        int dayOfWeek,
        int preferenceScore
) {
}
