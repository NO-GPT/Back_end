package com.example.new_portfolio_server.config;

import com.example.new_portfolio_server.config.redis.RedisServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 토큰으로 인증하고 인증정보를 SecurityContextHolder에 추가하는 역할을 담당한다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisServiceImpl redisServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰 검증
        if (StringUtils.hasText(token)) {
            // 블랙리스트 확인
            String blacklisted = redisServiceImpl.getValues("blacklist:" + token);
            if (blacklisted != null) {
                throw new JwtException("로그아웃된 토큰입니다.");
            }

            if (jwtTokenProvider.validateToken(token)) {
                try {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException e) {
                    throw new AuthenticationException(e.getMessage()) {};
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
