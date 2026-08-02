package com.covacova.member.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {
    public InvalidPasswordResetCodeException(String message) {
        super(message);
    }
}
