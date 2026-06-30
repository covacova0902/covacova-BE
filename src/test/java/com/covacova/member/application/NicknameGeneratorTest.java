package com.covacova.member.application;

import com.covacova.member.exception.NicknameGenerationException;
import com.covacova.member.infrastructure.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NicknameGeneratorTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private NicknameGenerator nicknameGenerator;

    @Test
    void 생성된_닉네임은_한글과_3자리_숫자_형태이다() {

        given(memberRepository.existsByNickname(anyString())).willReturn(false);

        String nickname = nicknameGenerator.generate();

        assertThat(nickname).matches("^[가-힣]+\\d{3}$");
    }

    @Test
    void 첫_번째_후보가_중복이면_재시도하여_성공한다() {

        given(memberRepository.existsByNickname(anyString()))
                .willReturn(true)
                .willReturn(false);

        String nickname = nicknameGenerator.generate();

        assertThat(nickname).isNotNull();

        //existsByNickname이 2번 호출됐는지 확인
        verify(memberRepository, times(2)).existsByNickname(anyString());
    }

    @Test
    void 최대_시도_횟수_초과_시_NicknameGenerationException을_던진다() {

        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        assertThatThrownBy(() -> nicknameGenerator.generate())
                .isInstanceOf(NicknameGenerationException.class);

        verify(memberRepository, times(10)).existsByNickname(anyString());
    }
}
