package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record RuleConfigurationUpdateRequest(@NotBlank String ruleValue) {
}
