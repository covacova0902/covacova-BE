package com.covacova.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldErrorResponse(
        @Schema(description = "검증에 실패한 필드명") String field,
        @Schema(description = "실패 사유") String message
) {
}
