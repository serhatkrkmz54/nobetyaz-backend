package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberPreferenceUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.MemberPreference;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftTemplate;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberPreferenceRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPreferenceService {

    private final MemberPreferenceRepository preferenceRepository;
    private final MemberRepository memberRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Member getMemberFromUser(UserDetailsImpl currentUser) {
        return memberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bu kullanıcıya ait personel kaydı bulunamadı. ID: " + currentUser.getId()
                ));
    }

    @Transactional(readOnly = true)
    public List<MemberPreferenceResponse> findByMember(UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        return preferenceRepository.findByMemberIdWithDetails(member.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberPreferenceResponse create(MemberPreferenceCreateRequest request, UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        ShiftTemplate shiftTemplate = shiftTemplateRepository.findById(request.shiftTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Nöbet Şablonu bulunamadı: " + request.shiftTemplateId()));

        MemberPreference preference = MemberPreference.builder()
                .member(member)
                .shiftTemplate(shiftTemplate)
                .dayOfWeek(request.dayOfWeek())
                .preferenceScore(request.preferenceScore())
                .build();

        preferenceRepository.save(preference);
        log.info("Yeni personel tercihi oluşturuldu: Member ID {}, Şablon ID {}, Gün {}",
                member.getId(), shiftTemplate.getId(), request.dayOfWeek());

        preference.setMember(member);
        preference.setShiftTemplate(shiftTemplate);
        return toResponse(preference);
    }

    @Transactional
    public MemberPreferenceResponse update(UUID preferenceId, MemberPreferenceUpdateRequest request, UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        MemberPreference preference = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tercih kaydı bulunamadı: " + preferenceId));

        if (!preference.getMember().getId().equals(member.getId())) {
            log.warn("Yetkisiz erişim denemesi: Kullanıcı {} (Member {}), Member {}'e ait tercihi (ID {}) güncellemeye çalıştı.",
                    currentUser.getUsername(), member.getId(), preference.getMember().getId(), preferenceId);
            throw new AccessDeniedException("Sadece kendi tercihlerinizi güncelleyebilirsiniz.");
        }

        preference.setPreferenceScore(request.preferenceScore());
        preferenceRepository.save(preference);
        log.info("Personel tercihi güncellendi: ID {}", preferenceId);

        return toResponse(preference);
    }

    @Transactional
    public void delete(UUID preferenceId, UserDetailsImpl currentUser) {
        Member member = getMemberFromUser(currentUser);
        MemberPreference preference = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tercih kaydı bulunamadı: " + preferenceId));

        if (!preference.getMember().getId().equals(member.getId())) {
            log.warn("Yetkisiz erişim denemesi: Kullanıcı {} (Member {}), Member {}'e ait tercihi (ID {}) silmeye çalıştı.",
                    currentUser.getUsername(), member.getId(), preference.getMember().getId(), preferenceId);
            throw new AccessDeniedException("Sadece kendi tercihlerinizi silebilirsiniz.");
        }

        preferenceRepository.delete(preference);
        log.info("Personel tercihi silindi: ID {}", preferenceId);
    }

    @Transactional(readOnly = true)
    public List<MemberPreference> findAllForSolver() {
        return preferenceRepository.findAllWithDetails();
    }

    private MemberPreferenceResponse toResponse(MemberPreference preference) {
        ShiftTemplate template = preference.getShiftTemplate();

        return new MemberPreferenceResponse(
                preference.getId(),
                preference.getMember().getId(),
                template.getId(),
                template.getName(),
                String.format("%s-%s",
                        template.getStartTime().format(TIME_FORMATTER),
                        template.getEndTime().format(TIME_FORMATTER)),
                preference.getDayOfWeek(),
                preference.getPreferenceScore()
        );
    }

}
