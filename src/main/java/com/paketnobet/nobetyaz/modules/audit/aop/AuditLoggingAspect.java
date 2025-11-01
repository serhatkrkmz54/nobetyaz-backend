package com.paketnobet.nobetyaz.modules.audit.aop;

import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.audit.entity.AuditLog;
import com.paketnobet.nobetyaz.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(pointcut = "@annotation(auditable)")
    public void logAfter(JoinPoint joinPoint, Auditable auditable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String actionType = auditable.actionType();
        String description = "Kullanıcı '" + username + "', " + joinPoint.getSignature().getName() + " işlemini gerçekleştirdi.";
        AuditLog log = AuditLog.builder()
                .timestamp(Instant.now())
                .username(username)
                .actionType(actionType)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }
}
