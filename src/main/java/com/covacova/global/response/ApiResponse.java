package com.covacova.global.response;

import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<FieldErrorResponse> errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static ApiResponse<Void> validationError(String message, List<FieldErrorResponse> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
