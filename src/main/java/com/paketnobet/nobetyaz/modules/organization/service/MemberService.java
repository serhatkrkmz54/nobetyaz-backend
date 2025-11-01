package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.DuplicateResourceException;
import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.Role;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.RoleRepository;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.MemberUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.QualificationDTO;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Qualification;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.QualificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final QualificationRepository qualificationRepository;
    private final RoleRepository roleRepository;

    String invitationToken = UUID.randomUUID().toString();
    Instant tokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);

    private String generateRandomPIN() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    @Transactional
    @Auditable(actionType = "CREATE_MEMBER")
    public MemberResponse create(MemberCreateRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Bu kullanıcı adı zaten alınmış: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu email adresi zaten kullanılıyor: " + request.email());
        }

        if (request.employeeId() != null && !request.employeeId().trim().isEmpty()) {
            if (memberRepository.existsByEmployeeId(request.employeeId())) {
                throw new DuplicateResourceException("Bu Personel Numarası (" + request.employeeId() + ") zaten sistemde kayıtlı.");
            }
        }

        Set<Role> userRoles = request.roles().stream()
                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName))
                        .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + roleName)))
                .collect(Collectors.toSet());

        String invitationPIN = generateRandomPIN();
        Instant tokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .password(null)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .roles(userRoles)
                .isActive(false)
                .passwordResetToken(invitationPIN)
                .tokenExpiry(tokenExpiry)
                .build();
        userRepository.save(newUser);

        Member member = Member.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .employeeId(request.employeeId())
                .user(newUser)
                .isActive(true)
                .build();

        memberRepository.save(member);

        List<QualificationDTO> qualificationDTOs = member.getQualifications().stream()
                .map(q -> new QualificationDTO(q.getId(), q.getName()))
                .toList();

        return new MemberResponse(
                member.getId(),
                newUser.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getPhoneNumber(),
                member.getEmployeeId(),
                member.isActive(),
                qualificationDTOs,
                "DAVET_BEKLIYOR",
                invitationPIN,
                newUser.getUsername()
        );
    }

    @Transactional
    public MemberResponse resendInvitation(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı: " + memberId));

        User user = member.getUser();
        if (user == null) {
            throw new RuleViolationException("Bu personel için bir kullanıcı hesabı bulunmuyor.");
        }

        if (user.getPassword() != null) {
            throw new RuleViolationException("Bu kullanıcı zaten şifresini belirlemiş ve aktif.");
        }

        String newInvitationPIN = generateRandomPIN();
        Instant newTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);

        user.setPasswordResetToken(newInvitationPIN);
        user.setTokenExpiry(newTokenExpiry);
        user.setActive(false);
        userRepository.save(user);

        log.info("Personel için yeni davet token'ı oluşturuldu: {}", member.getFirstName());

        return toResponse(member);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse findById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return toResponse(member);
    }

    public Optional<Member> findByUserId(UUID userId) {
        return memberRepository.findByUserId(userId);
    }

    @Transactional
    public MemberResponse update(UUID id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));
        }

        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setPhoneNumber(request.phoneNumber());
        member.setEmployeeId(request.employeeId());
        member.setActive(request.isActive());
        member.setUser(user);

        if (request.qualificationIds() != null) {
            List<Qualification> qualifications = qualificationRepository.findAllById(request.qualificationIds());
            member.setQualifications(new HashSet<>(qualifications));
        } else {
            member.getQualifications().clear();
        }

        memberRepository.save(member);
        return toResponse(member);
    }

    @Transactional
    @Auditable(actionType = "DELETE_MEMBER")
    public void delete(UUID id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
    }

    public MemberResponse toResponse(Member member) {
        List<QualificationDTO> qualificationDTOs = member.getQualifications().stream()
                .map(q -> new QualificationDTO(q.getId(), q.getName()))
                .toList();

        String userStatus = "HESAP_YOK";
        String invitationToken = null;
        UUID memberUserId = null;
        String username = null;

        if (member.getUser() != null) {
            User user = member.getUser();
            memberUserId = user.getId();
            username = user.getUsername();

            if (user.getPassword() != null) {
                userStatus = "AKTIF";
            } else if (user.getPasswordResetToken() != null && user.getTokenExpiry() != null && user.getTokenExpiry().isAfter(Instant.now())) {
                userStatus = "DAVET_BEKLIYOR";
                invitationToken = user.getPasswordResetToken();
            } else {
                userStatus = "SURESI_DOLMUS";
            }
        }

        return new MemberResponse(
                member.getId(),
                member.getUser() != null ? member.getUser().getId() : null,
                member.getFirstName(),
                member.getLastName(),
                member.getPhoneNumber(),
                member.getEmployeeId(),
                member.isActive(),
                qualificationDTOs,
                userStatus,
                invitationToken,
                username
        );
    }

}
