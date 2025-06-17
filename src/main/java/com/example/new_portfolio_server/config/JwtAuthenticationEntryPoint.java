package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String token = request.getHeader("Authorization");
        String tokenInfo = token != null ? token.substring(0, Math.min(10, token.length())) : "없음";

        // Swagger 경로 무시
        if (requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/swagger-ui")) {
            return;
        }

        // 상세 인증 오류 메시지
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("uri", requestURI);
        errorDetails.put("token", tokenInfo);

        String errorMessage = authException.getMessage();
        if (errorMessage.contains("expired")) {
            errorDetails.put("error", "토큰 만료");
            errorDetails.put("message", "토큰이 만료되었습니다. 새 토큰을 발급받아 주세요.");
        } else if (errorMessage.contains("invalid") || errorMessage.contains("malformed")) {
            errorDetails.put("error", "토큰 유효성 검사 실패");
            errorDetails.put("message", "유효하지 않은 토큰입니다. 토큰 형식을 확인해 주세요.");
        } else if (errorMessage.contains("Full authentication is required")) {
            errorDetails.put("error", "인증 필요");
            errorDetails.put("message", "인증 토큰이 필요합니다. 'Bearer <token>' 형식을 사용하세요.");
        } else {
            errorDetails.put("error", "알 수 없는 인증 오류");
            errorDetails.put("message", "인증 과정에서 오류가 발생했습니다: " + errorMessage);
        }

        log.error("인증 실패: URI: {}, 토큰: {}, 상세: {}", requestURI, tokenInfo, errorDetails);

        ApiResponse<Object> apiResponse = ApiResponse.error(errorDetails.toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}