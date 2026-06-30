package com.covacova.member.presentation;

import com.covacova.global.response.ApiResponse;
import com.covacova.member.application.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원관리 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        Long memberId = memberService.signup(
                request.email(),
                request.password(),
                request.privacyAgreed(),
                request.termsAgreed()
        );

        return ApiResponse.success(new SignupResponse(memberId));
    }
}
