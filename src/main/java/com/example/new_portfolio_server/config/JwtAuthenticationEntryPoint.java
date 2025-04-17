package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// nestjs의 가드 같은 역할?
// 유효한 자격증명을 제공하지 않고 접근하려 할때 401 Unauthorized 에러를 리턴한다.
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException authException) throws IOException, ServletException {
        String requestURI = req.getRequestURI();
        String token = req.getHeader("Authorization");
        String tokenInfo = token != null ? token.substring(0, Math.min(10, token.length())) : "없음";
        String message = authException.getMessage();

        log.error("인증 실패: URI: {}, 토큰: {}, 메시지: {}", requestURI, tokenInfo, message);
        ApiResponse<Object> response = ApiResponse.error(message);

        // HTTP 응답 설정
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // JSON으로 변환하여 응답 작성
        res.getWriter().write(objectMapper.writeValueAsString(response));
    }
}
