package com.example.new_portfolio_server.auth;

import com.example.new_portfolio_server.auth.Dto.RequestLogin;
import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.example.new_portfolio_server.common.Response.JWTAuthResponse;
import com.example.new_portfolio_server.config.JwtTokenProvider;
import com.example.new_portfolio_server.user.MyUserDetailsService;
import com.example.new_portfolio_server.user.User;
import com.example.new_portfolio_server.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 대신 PasswordEncoder 사용

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

    public ApiResponse<Optional<User>> verifyToken(String bearerToken) {
        // 토큰이 없거나 Bearer 형식이 아닌 경우 예외 던지기
        if (bearerToken == null || !bearerToken.startsWith(JwtTokenProvider.BEARER + " ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다");
        }

        String token = bearerToken.substring(7);

        // 토큰 유효성 검사 및 사용자 이름 추출 (예외는 JwtTokenProvider에서 발생)
        String username = jwtTokenProvider.getUsername(token);
        Optional<User> user = userRepository.findByUsername(username);

        return ApiResponse.success(user);
    }
}
