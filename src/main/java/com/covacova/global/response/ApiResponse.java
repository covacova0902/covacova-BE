package com.covacova.global.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
