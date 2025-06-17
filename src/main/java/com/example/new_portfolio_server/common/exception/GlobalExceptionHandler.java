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

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String token = request.getHeader("Authorization") != null ? request.getHeader("Authorization").substring(0, Math.min(10, request.getHeader("Authorization").length())) : "없음";
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("uri", requestURI);
        errorDetails.put("token", token);
        errorDetails.put("error", "JWT 오류");
        errorDetails.put("message", "JWT 처리 중 오류가 발생했습니다: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorDetails.toString()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String token = request.getHeader("Authorization") != null ? request.getHeader("Authorization").substring(0, Math.min(10, request.getHeader("Authorization").length())) : "없음";
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("uri", requestURI);
        errorDetails.put("token", token);
        errorDetails.put("error", "인증 실패");
        errorDetails.put("message", "인증 실패: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorDetails.toString()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestPartException(MissingServletRequestPartException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String missingPart = ex.getRequestPartName();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("uri", requestURI);
        errorDetails.put("error", "필수 파트 누락");
        errorDetails.put("missingField", missingPart);
        errorDetails.put("message", String.format("'%s' 필드가 누락되었습니다. 올바른 멀티파트 요청을 확인하세요.", missingPart));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorDetails.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("uri", requestURI);
        errorDetails.put("error", "내부 서버 오류");
        errorDetails.put("message", "서버에서 처리 중 오류가 발생했습니다: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorDetails.toString()));
    }
}