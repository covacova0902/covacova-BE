package com.covacova.member.application;

import com.covacova.member.domain.Member;
import com.covacova.member.exception.DuplicateEmailException;
import com.covacova.member.infrastructure.MemberRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameGenerator nicknameGenerator;

    public Long signup(String email, String password, boolean privacyAgreed, boolean termsAgreed) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Member member = Member.create(
                email,
                passwordEncoder.encode(password),
                privacyAgreed,
                termsAgreed,
                nicknameGenerator.generate()
        );

        return memberRepository.save(member).getMemberId();
    }
}
