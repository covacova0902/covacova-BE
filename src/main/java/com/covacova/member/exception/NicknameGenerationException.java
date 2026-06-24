package com.covacova.member.exception;

public class NicknameGenerationException extends RuntimeException {

    public NicknameGenerationException() {
        super("닉네임 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}
