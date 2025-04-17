package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.example.new_portfolio_server.config.redis.RedisServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 토큰으로 인증하고 인증정보를 SecurityContextHolder에 추가하는 역할을 담당한다.
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisServiceImpl redisServiceImpl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);
        String requestURI = request.getRequestURI();

        if (StringUtils.hasText(token)) {
            try {
                // 블랙리스트 확인
                String blacklisted = redisServiceImpl.getValues("blacklist:" + token);
                if (blacklisted != null) {
                    log.warn("블랙리스트 토큰 감지: {}, URI: {}", token.substring(0, Math.min(10, token.length())), requestURI);
                    sendErrorResponse(response, "로그아웃된 토큰입니다.");
                    return;
                }

                // 토큰 유효성 검증
                try {
                    if (!jwtTokenProvider.validateToken(token)) {
                        log.warn("토큰 검증 실패: {}, URI: {}", token.substring(0, Math.min(10, token.length())), requestURI);
                        sendErrorResponse(response, "유효하지 않은 토큰입니다.");
                        return;
                    }
                } catch (JwtException e) {
                    log.error("토큰 검증 오류: {}, URI: {}, 메시지: {}", token.substring(0, Math.min(10, token.length())), requestURI, e.getMessage());
                    sendErrorResponse(response, e.getMessage()); // validateToken의 상세 메시지
                    return;
                }

                // 토큰 타입 확인 및 처리
                String tokenType = jwtTokenProvider.getTokenType(token);
                if ("refresh".equals(tokenType) && !"/auth/reissue".equals(requestURI)) {
                    log.warn("리프레시 토큰 오용 감지: {}, URI: {}", token.substring(0, Math.min(10, token.length())), requestURI);
                    sendErrorResponse(response, "리프레시 토큰은 이 엔드포인트(" + requestURI + ")에 사용할 수 없습니다.");
                    return;
                }

                // 액세스 토큰 인증
                if ("access".equals(tokenType)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("액세스 토큰 인증 성공: {}, URI: {}", token.substring(0, Math.min(10, token.length())), requestURI);
                }
            } catch (Exception e) {
                log.error("예상치 못한 오류: {}, URI: {}, 메시지: {}", token.substring(0, Math.min(10, token.length())), requestURI, e.getMessage());
                sendErrorResponse(response, "인증 처리 중 오류가 발생했습니다: " + e.getMessage());
                return;
            }
        } else {
            log.debug("토큰 없음: URI: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        ApiResponse<Object> apiResponse = ApiResponse.error(message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}