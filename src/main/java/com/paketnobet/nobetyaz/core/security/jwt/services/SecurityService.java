package com.paketnobet.nobetyaz.core.security.jwt.services;

import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final MemberRepository memberRepository;
    private final RuleConfigurationRepository ruleConfigRepository;

    public boolean isOwnerOfMember(UUID memberId, UserDetailsImpl principal) {
        if (memberId == null || principal == null) {
            return false;
        }

        return memberRepository.findById(memberId)
                .map(member -> member.getUser() != null && member.getUser().getId().equals(principal.getId()))
                .orElse(false);
    }

    public boolean isFeatureEnabled(String featureKey) {
        return ruleConfigRepository.findById(featureKey)
                .map(config -> config.getRuleValue().equalsIgnoreCase("true"))
                .orElse(false);
    }
}
