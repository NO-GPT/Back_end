package com.example.new_portfolio_server.auth;

import com.example.new_portfolio_server.auth.Dto.RequestLogin;
import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.example.new_portfolio_server.common.Response.JWTAuthResponse;
import com.example.new_portfolio_server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTAuthResponse>> loginWithValidation(@RequestBody RequestLogin requestLogin) {
        ApiResponse<JWTAuthResponse> response = authService.loginWithValidation(requestLogin);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Optional<User>>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        ApiResponse<Optional<User>> response = authService.verifyToken(token);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
