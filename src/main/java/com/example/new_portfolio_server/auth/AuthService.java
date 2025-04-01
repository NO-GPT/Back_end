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

    public ApiResponse<User> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        return ApiResponse.success(user);
    }
}
