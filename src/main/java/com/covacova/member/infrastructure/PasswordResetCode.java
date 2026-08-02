package com.covacova.member.infrastructure;

public record PasswordResetCode(String code, int attemptCount) {
}
