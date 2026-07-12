package com.covacova.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessTokenResponse(
        @Schema(description = "액세스 토큰") String accessToken
) {
}