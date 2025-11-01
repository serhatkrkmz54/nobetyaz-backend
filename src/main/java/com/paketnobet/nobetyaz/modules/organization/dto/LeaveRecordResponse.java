package com.paketnobet.nobetyaz.modules.organization.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveRecordResponse(UUID id,
                                  UUID memberId,
                                  String memberFirstName,
                                  String memberLastName,
                                  String leaveType,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  String reason,
                                  String status) {
}
