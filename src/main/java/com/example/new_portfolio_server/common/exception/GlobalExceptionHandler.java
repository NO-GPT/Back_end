package com.example.new_portfolio_server.common.exception;

import com.example.new_portfolio_server.common.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// 컨트롤러에서 이루어지는 작업
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        String missingPart = ex.getRequestPartName();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "필수 파트 누락");
        errorDetails.put("missingField", missingPart);
        errorDetails.put("message", String.format("'%s' 필드가 누락되었습니다. 올바른 멀티파트 요청을 확인하세요.", missingPart));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorDetails.toString()));
    }
    // null 검사
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Long>> handleNullPointerException(NullPointerException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse
                        .error(ex.getMessage()));
    }

    // 중복성 검사
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Long>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse
                        .error(ex.getMessage()));
    }

    // dto 유효성 검사
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Long>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", ")); // 유효성 검사 실패 메시지를  하나의 문자열로 결합.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    // JWT 예외 처리
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException ex) {
        ApiResponse<Object> response = ApiResponse
                .error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response); // 401 Unauthorized
    }

    // AuthenticationException 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse
                        .error("인증 실패: " + ex.getMessage()));
    }
}