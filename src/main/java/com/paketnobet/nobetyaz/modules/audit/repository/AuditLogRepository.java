package com.paketnobet.nobetyaz.modules.audit.repository;

import com.paketnobet.nobetyaz.modules.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository <AuditLog, UUID> {
}
