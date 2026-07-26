package com.covacova.member.application;

import com.covacova.global.mail.EmailSender;
import com.covacova.member.infrastructure.MemberRepository;
import com.covacova.member.infrastructure.PasswordResetCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int CODE_BOUND = 1_000_000;

    private static final String MAIL_SUBJECT = "[코바코바] 비밀번호 재설정 인증코드";

    private final MemberRepository memberRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(readOnly = true)
    public void requestReset(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> issueAndSendCode(email));
    }

    private void issueAndSendCode(String email) {
        String code = generateCode();
        passwordResetCodeRepository.save(email, code);
        emailSender.send(email, MAIL_SUBJECT, "인증코드: " + code + " (10분 이내에 입력해주세요.)");
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(CODE_BOUND));
    }
}
