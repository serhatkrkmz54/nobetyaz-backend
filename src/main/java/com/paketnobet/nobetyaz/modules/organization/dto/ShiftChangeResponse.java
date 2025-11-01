package com.paketnobet.nobetyaz.modules.organization.dto;

import java.util.UUID;

public record ShiftChangeResponse(UUID id,
                                  ShiftInfo initiatingShift,
                                  MemberInfo initiatingMember,
                                  ShiftInfo targetShift,
                                  MemberInfo targetMember,
                                  String status,
                                  String requestReason,
                                  String resolutionNotes) {
}
