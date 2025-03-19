package com.example.new_portfolio_server.user;

import com.example.new_portfolio_server.common.Response.ApiResponse;
import com.example.new_portfolio_server.user.Dto.CreateUserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
