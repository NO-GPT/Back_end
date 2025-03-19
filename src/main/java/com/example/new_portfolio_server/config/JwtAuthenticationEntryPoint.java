package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// nestjs의 가드 같은 역할?
// 유효한 자격증명을 제공하지 않고 접근하려 할때 401 Unauthorized 에러를 리턴한다.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException authException) throws IOException, ServletException {
        ApiResponse<Object> response = ApiResponse.error("인증 실패: " + authException.getMessage());

        // HTTP 응답 설정
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // JSON으로 변환하여 응답 작성
        res.getWriter().write(objectMapper.writeValueAsString(response));
    }
}
