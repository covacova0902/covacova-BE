package com.covacova.member.application;

import com.covacova.member.domain.Member;
import com.covacova.member.exception.DuplicateEmailException;
import com.covacova.member.infrastructure.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

//스프링 없이 테스트(순수 자바 로직이라)
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NicknameGenerator nicknameGenerator;

    //Mock으로 만든 가짜들을 자동으로 생성자에 주입
    @InjectMocks
    private MemberService memberService;

    @Test
    void 정상_회원가입_시_memberId를_반환한다() {

        Member savedMember = mock(Member.class);
        given(savedMember.getMemberId()).willReturn(1L);

        given(memberRepository.existsByEmail("test@example.com")).willReturn(false);

        given(nicknameGenerator.generate()).willReturn("귀여운털뭉치001");

        given(passwordEncoder.encode("password123")).willReturn("encoded");

        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        Long result = memberService.signup("test@example.com", "password123", true, true);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void 이메일이_중복이면_DuplicateEmailException을_던지고_저장하지_않는다() {

        //중복 있다고 설정
        given(memberRepository.existsByEmail("test@example.com")).willReturn(true);

        //signup 실행하면 DuplicateEmailException을 던지는지 확인
        assertThatThrownBy(() -> memberService.signup("test@example.com", "password123", true, true))
                .isInstanceOf(DuplicateEmailException.class);

        //save 메소드가 한 번도 호출 안됐는지 확인
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 비밀번호는_인코딩되어_저장된다() {

        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(nicknameGenerator.generate()).willReturn("귀여운털뭉치001");
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(memberRepository.save(any(Member.class))).willReturn(mock(Member.class));

        memberService.signup("test@example.com", "password123", true, true);

        //save에 어떤 Member 객체가 들어갔는지 캡처하는 도구
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepository).save(captor.capture());

        //캡처된 Member의 패스워드가 평문인지 암호화된 값인지 확인
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void 이메일이_존재하지_않으면_사용가능하다고_반환한다() {

        given(memberRepository.existsByEmail("test@example.com")).willReturn(false);

        boolean result = memberService.isEmailAvailable("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void 이메일이_이미_존재하면_사용불가능하다고_반환한다() {

        given(memberRepository.existsByEmail("test@example.com")).willReturn(true);

        boolean result = memberService.isEmailAvailable("test@example.com");

        assertThat(result).isFalse();
    }
}
