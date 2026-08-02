package com.covacova.member.presentation;

import com.covacova.global.response.ApiResponse;
import com.covacova.member.application.PasswordResetService;
import com.covacova.member.dto.request.PasswordResetRequest;
import com.covacova.member.dto.response.PasswordResetResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "비밀번호 재설정", description = "이메일 인증코드를 통한 비밀번호 재설정 API")
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private static final String RESET_REQUEST_MESSAGE = "입력하신 이메일 주소로 인증코드를 보내드렸습니다. " +
            "메일이 오지 않는다면 가입 시 사용한 이메일이 맞는지 확인해주세요.";

    private final PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    public ApiResponse<PasswordResetResponse> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());

        return ApiResponse.success(new PasswordResetResponse(RESET_REQUEST_MESSAGE));
    }
}
