package com.example.new_portfolio_server.auth;

import com.example.new_portfolio_server.auth.Dto.RequestLogin;
import com.example.new_portfolio_server.common.response.ApiResponse;
import com.example.new_portfolio_server.common.response.JWTAuthResponse;
import com.example.new_portfolio_server.config.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth API", description = "로그인/인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTAuthResponse>> loginWithValidation(@RequestBody RequestLogin requestLogin) {
        ApiResponse<JWTAuthResponse> response = authService.loginWithValidation(requestLogin);

        if (response.isSuccess()) {
            return ResponseEntity
                    .ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile() {
        return ResponseEntity
                .ok(authService.getProfileWithPortfolios());
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> reissue(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request); // Refresh 헤더에서 리프레시 토큰 추출
        ApiResponse<String> response = authService.reissueAccessToken(refreshToken);

        if (response.isSuccess()) {
            return ResponseEntity
                    .ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request); // Authorization 헤더에서 액세스 토큰 추출
        ApiResponse<String> response = authService.logout(accessToken);

        if (response.isSuccess()) {
            return ResponseEntity
                    .ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }
}
