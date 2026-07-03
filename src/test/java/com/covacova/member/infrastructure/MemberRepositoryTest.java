package com.covacova.member.infrastructure;

import com.covacova.global.domain.JpaAuditingConfig;
import com.covacova.member.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 저장된_이메일로_조회하면_true를_반환한다() {
        memberRepository.save(Member.create("test@example.com", "encoded", true, true, "귀여운털뭉치001"));

        assertThat(memberRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    void 없는_이메일로_조회하면_false를_반환한다() {
        assertThat(memberRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void 저장된_닉네임으로_조회하면_true를_반환한다() {
        memberRepository.save(Member.create("test@example.com", "encoded", true, true, "귀여운털뭉치001"));
        assertThat(memberRepository.existsByNickname("귀여운털뭉치001")).isTrue();
    }

    @Test
    void 없는_닉네임으로_조회하면_false를_반환한다() {
        assertThat(memberRepository.existsByNickname("없는닉네임999")).isFalse();
    }


}
