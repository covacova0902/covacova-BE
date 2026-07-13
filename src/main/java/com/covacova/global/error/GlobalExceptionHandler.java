package com.covacova.global.error;

import com.covacova.global.response.ApiResponse;
import com.covacova.global.response.FieldErrorResponse;
import com.covacova.member.exception.DuplicateEmailException;
import com.covacova.member.exception.InvalidRefreshTokenException;
import com.covacova.member.exception.NicknameGenerationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//모든 컨트롤러 예외를 여기서 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Valid 검증 실패 시 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<FieldErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()),
                        (first, second) -> first,
                        LinkedHashMap::new))
                .values().stream().toList();

        return ResponseEntity.badRequest().body(ApiResponse.validationError("입력값을 확인해주세요.", errors));
    }

    //이메일 중복 예외
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
    }

    //닉네임 자동생성 실패 예외
    @ExceptionHandler(NicknameGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleNicknameGeneration(NicknameGenerationException e) {
        log.error("닉네임 생성 실패", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
    }

    //파라미터 단위 검증 실패 시 예외
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        List<FieldErrorResponse> errors = e.getParameterValidationResults().stream()
                .map(result -> new FieldErrorResponse(
                        result.getMethodParameter().getParameterName(),
                        result.getResolvableErrors().get(0).getDefaultMessage())).toList();

        return ResponseEntity.badRequest().body(ApiResponse.validationError("입력값을 확인해주세요.", errors));
    }

    //이메일 또는 비밀번호 실패 예외
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    //탈퇴한 회원 예외
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocked(LockedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("탈퇴한 회원입니다."));
    }

    //리프레시 토큰 검증 실패 예외
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
    }

    //위에서 잡지 못한 나머지 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
