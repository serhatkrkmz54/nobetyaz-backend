package com.paketnobet.nobetyaz.modules.audit.service;

import com.paketnobet.nobetyaz.modules.audit.entity.AuditLog;
import com.paketnobet.nobetyaz.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> findAll() { return auditLogRepository.findAll(); }
}
