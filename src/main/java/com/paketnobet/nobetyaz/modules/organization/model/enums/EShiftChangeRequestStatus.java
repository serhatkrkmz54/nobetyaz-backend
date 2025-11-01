package com.paketnobet.nobetyaz.modules.organization.model.enums;

public enum EShiftChangeRequestStatus {
    PENDING_TARGET_APPROVAL,  // Hedef personelin onayı bekleniyor
    PENDING_MANAGER_APPROVAL, // Yöneticinin onayı bekleniyor
    APPROVED,                 // Onaylandı
    REJECTED,                 // Reddedildi
    CANCELLED                 // İptal edildi
}
