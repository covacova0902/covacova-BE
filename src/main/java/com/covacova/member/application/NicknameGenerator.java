package com.covacova.member.application;

import com.covacova.member.exception.NicknameGenerationException;
import com.covacova.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "귀여운", "꼼꼼한", "대단한", "용감한", "씩씩한",
            "엉뚱한", "수줍은", "똑똑한", "따뜻한", "재빠른",
            "포근한", "신나는", "차분한", "발랄한", "느긋한",
            "활발한", "다정한", "엄청난", "깜찍한", "진지한"
    );

    private static final List<String> NOUNS = List.of(
            "털뭉치", "코바늘", "대바늘", "실타래", "뜨개질",
            "양모볼", "코잡기", "사슬코", "짧은뜨기", "긴뜨기",
            "돗바늘", "단수링", "실감개", "니트냥", "목도리", "털장갑"
    );

    private static final int MAX_NUMBER = 1000;

    //무한루프 방지용 시도 횟수
    private static final int MAX_ATTEMPTS = 10;

    private final MemberRepository memberRepository;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        for (int attempt=0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = generateCandidate();

            if (!memberRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }

        throw new NicknameGenerationException();
    }

    private String generateCandidate() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        String number = String.format("%03d", random.nextInt(MAX_NUMBER));
        return adjective + noun + number;
    }
}
