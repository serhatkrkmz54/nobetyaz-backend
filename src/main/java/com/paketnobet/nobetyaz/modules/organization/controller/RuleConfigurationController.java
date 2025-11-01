package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.RuleConfigurationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.RuleConfigurationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.RuleConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/rules")
@RequiredArgsConstructor
public class RuleConfigurationController {

    private final RuleConfigurationService ruleConfigurationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<RuleConfigurationResponse>> getAllRules() {
        return ResponseEntity.ok(ruleConfigurationService.findAll());
    }

    @PutMapping("/{ruleKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RuleConfigurationResponse> updateRule(
            @PathVariable String ruleKey,
            @Valid @RequestBody RuleConfigurationUpdateRequest request) {
        return ResponseEntity.ok(ruleConfigurationService.update(ruleKey, request));
    }
}

