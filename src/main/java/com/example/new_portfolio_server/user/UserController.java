package com.example.new_portfolio_server.user;

import com.example.new_portfolio_server.common.response.ApiResponse;
import com.example.new_portfolio_server.user.dto.CreateUserDto;
import com.example.new_portfolio_server.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        List<User> users =  userService.getUsers();
        return ResponseEntity
                .ok(ApiResponse
                        .success(users));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> createUser(@RequestBody @Valid CreateUserDto dto) {
        Long userId = userService.createUser(dto);

        return ResponseEntity
                .ok(ApiResponse
                        .successLong("회원가입 성공", userId));
    }
}
