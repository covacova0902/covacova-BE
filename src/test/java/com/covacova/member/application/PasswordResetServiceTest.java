package com.covacova.member.application;

import com.covacova.global.mail.EmailSender;
import com.covacova.member.domain.Member;
import com.covacova.member.infrastructure.MemberRepository;
import com.covacova.member.infrastructure.PasswordResetCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void 존재하는_이메일이면_코드를_저장하고_메일을_발송한다() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mock(Member.class)));

        passwordResetService.requestReset("test@example.com");

        verify(passwordResetCodeRepository).save(eq("test@example.com"), anyString());
        verify(emailSender).send(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void 생성된_인증코드는_6자리_숫자이다() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mock(Member.class)));

        passwordResetService.requestReset("test@example.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetCodeRepository).save(eq("test@example.com"), codeCaptor.capture());

        assertThat(codeCaptor.getValue()).matches("^\\d{6}$");
    }

    @Test
    void 존재하지_않는_이메일이면_아무것도_하지_않는다() {
        given(memberRepository.findByEmail("no-such@example.com"))
                .willReturn(Optional.empty());

        passwordResetService.requestReset("no-such@example.com");

        verify(passwordResetCodeRepository, never()).save(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}
