package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.organization.dto.RuleConfigurationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.RuleConfigurationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.RuleConfiguration;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleConfigurationService {

    private final RuleConfigurationRepository ruleConfigurationRepository;

    public List<RuleConfigurationResponse> findAll() {
        return ruleConfigurationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(actionType = "UPDATE_RULE")
    public RuleConfigurationResponse update(String ruleKey, RuleConfigurationUpdateRequest request) {
        RuleConfiguration rule = ruleConfigurationRepository.findById(ruleKey)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with key: " + ruleKey));

        rule.setRuleValue(request.ruleValue());
        ruleConfigurationRepository.save(rule);

        return toResponse(rule);
    }

    private RuleConfigurationResponse toResponse(RuleConfiguration rule) {
        return new RuleConfigurationResponse(
                rule.getRuleKey(),
                rule.getRuleValue(),
                rule.getDescription()
        );
    }
}
