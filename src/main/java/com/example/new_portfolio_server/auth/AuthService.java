package com.example.new_portfolio_server.auth;

import com.example.new_portfolio_server.auth.Dto.RequestLogin;
import com.example.new_portfolio_server.common.response.ApiResponse;
import com.example.new_portfolio_server.common.response.JWTAuthResponse;
import com.example.new_portfolio_server.config.JwtTokenProvider;
import com.example.new_portfolio_server.config.redis.RedisServiceImpl;
import com.example.new_portfolio_server.user.MyUserDetailsService;
import com.example.new_portfolio_server.user.entity.User;
import com.example.new_portfolio_server.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;      // BCryptPasswordEncoder 대신 PasswordEncoder 사용
    private final RedisServiceImpl redisServiceImpl;

    public JWTAuthResponse login(RequestLogin loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long userId = myUserDetailsService.findUserIdByUsername(loginRequest.getUsername());

        JWTAuthResponse token = jwtTokenProvider.generateToken(loginRequest.getUsername(), authentication, userId);

        return token;
    }

    public ApiResponse<JWTAuthResponse> loginWithValidation(RequestLogin loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);
        if (user == null) {
            return ApiResponse.error("아이디를 다시 확인해주세요.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ApiResponse.error("비밀번호를 다시 확인해주세요.");
        }

        JWTAuthResponse token = login(loginRequest);

        return ApiResponse.success("로그인 성공", token);
    }

    public ApiResponse<Map<String, Object>> getProfileWithPortfolios() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 사용자와 작성한 포트폴리오를 함께 반환
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("portfolios", user.getPortfolios());

        return ApiResponse.success(response);
    }

    public ApiResponse<String> reissueAccessToken(String refreshToken) {
        // 헤더에 리프레시 토큰이 없으면??
        if (!StringUtils.hasText(refreshToken)) {
            return ApiResponse.error("리프레시 토큰이 제공되지 않았습니다.");
        }

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ApiResponse.error("유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰 타입 확인
            if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
                return ApiResponse.error("리프레시 토큰이 아닙니다.");
            }

            // 토큰에서 사용자 이름 추출
            String username = jwtTokenProvider.getUsername(refreshToken);

            // Redis에 저장된 리프레시 토큰과 비교
            String storedRefreshToken = redisServiceImpl.getValues(username);
            if (!refreshToken.equals(storedRefreshToken)) {
                return ApiResponse.error("리프레시 토큰이 일치하지 않습니다.");
            }

            // 새로운 access 토큰 생성
            Long userId = myUserDetailsService.findUserIdByUsername(username);
            String newAccessToken = jwtTokenProvider.generateAccessToken(username, userId);

            //(액세스 토큰만 반환
            return ApiResponse.success("액세스 토큰 재발급 성공", newAccessToken);

        } catch (JwtException e) {
            return ApiResponse.error("리프레시 토큰 처리 중 오류: " + e.getMessage());
        }
    }

    public ApiResponse<String> logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return ApiResponse.error("액세스 토큰이 제공되지 않았습니다.");
        }

        try {
            //액세스 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(accessToken)) {
                return ApiResponse.error("유효하지 않은 액세스 토큰입니다.");
            }

            // 토큰 타입 확인
            if (!"access".equals(jwtTokenProvider.getTokenType(accessToken))) {
                return ApiResponse.error("액세스 토큰이 아닙니다.");
            }

            //사용자 이름 추출
            String username = jwtTokenProvider.getUsername(accessToken);

            // Redis에서 리프레시 토큰 삭제
            redisServiceImpl.deleteValues(username);

            // 액세스 토큰을 블랙리스트에 추가
            // 액세스 토큰의 남은 유효 시간을 계산
            Long expiration = jwtTokenProvider.getAccessTokenExpiration(accessToken);
            Long now = System.currentTimeMillis();
            Long ttl = expiration - now; // 남은 유효 시간 (ms)
            if (ttl > 0) {
                redisServiceImpl.setValues("blacklist:" + accessToken, "logout", Duration.ofMillis(ttl));
            }

            return ApiResponse.success("로그아웃 성공", null);

        } catch (JwtException e) {
            return ApiResponse.error("로그아웃 처리 중 오류: " + e.getMessage());
        }
    }
}
