package com.example.new_portfolio_server.user;

import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.common.response.ApiResponse;
import com.example.new_portfolio_server.user.dto.CreateUserDto;
import com.example.new_portfolio_server.user.dto.ResponseUserDto;
import com.example.new_portfolio_server.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        List<User> users =  userService.getUsers();

        return ResponseEntity
                .ok(ApiResponse
                        .success(users));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<ResponseUserDto>> getUserById(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("사용자가 존재하지 않습니다."));
        }
        ResponseUserDto user = userService.getUserById(id);
        return ResponseEntity
                .ok(ApiResponse
                        .success(user));
    }

    @Operation(summary = "회원가입", description = "사용자 추가")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> createUser(@RequestBody @Valid CreateUserDto dto) {
        Long userId = userService.createUser(dto);

        return ResponseEntity
                .ok(ApiResponse
                        .successLong("회원가입 성공", userId));
    }
}
