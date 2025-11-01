package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScheduleGenerateRequest(@Min(2020) int year, @Min(1) @Max(12) int month) {}
