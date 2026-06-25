package com.covacova.member.presentation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 64)
        String password,

        @AssertTrue(message = "이용약관에 동의해야 합니다.")
        boolean termsAgreed,

        @AssertTrue(message = "개인정보 처리방침에 동의해야 합니다.")
        boolean privacyAgreed
) {
}
