package com.covacova.member.domain;

import com.covacova.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false, updatable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_level_id")
    private SkillLevel skillLevel;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "privacy_agreed", nullable = false)
    private boolean privacyAgreed;

    @Column(name = "terms_agreed", nullable = false)
    private boolean termsAgreed;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    public void completeOnboarding(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
        this.onboardingCompleted = true;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String avatarUrl) {
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            String email,
            String password,
            MemberStatus status,
            boolean privacyAgreed,
            boolean termsAgreed,
            boolean onboardingCompleted,
            String nickname,
            String avatarUrl
    ) {
        this.email = email;
        this.password = password;
        this.status = status;
        this.privacyAgreed = privacyAgreed;
        this.termsAgreed = termsAgreed;
        this.onboardingCompleted = onboardingCompleted;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public static Member create(String email, String password, boolean privacyAgreed, boolean termsAgreed, String nickname) {
        return Member.builder()
                .email(email)
                .password(password)
                .status(MemberStatus.PENDING)
                .privacyAgreed(privacyAgreed)
                .termsAgreed(termsAgreed)
                .onboardingCompleted(false)
                .nickname(nickname)
                .build();
    }
}
