package com.example.new_portfolio_server.user.dto;

import com.example.new_portfolio_server.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "사용자 응답 DTO")
public class ResponseUserDto {
    @Schema(description = "사용자 아이디", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "test")
    private String username;

    @Schema(description = "비밀번호", example = "1qaz2wsx!")
    private String password;

    @Schema(description = "이메일", example = "test@test.com   ")
    private String email;

    @Schema(description = "실명", example = "김승환")
    private String fullName;

    @Schema(description = "분야", example = "백엔드")
    private String field;

    @Schema(description = "소속", example = "취준생")
    private String group;

    @Schema(description = "기술 스택", example = "Java, Spring, React")
    private String stack;

    @Schema(description = "깃헙 아이디", example = "silofn523")
    private String githubId;

    @Schema(description = "프사 url", example = "https://example.com/profile.jpg")
    private String profile;

    public static ResponseUserDto from(User user) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .field(user.getField())
                .group(user.getGroup())
                .stack(user.getStack())
                .githubId(user.getGithubId())
                .profile(user.getProfile())
                .build();
    }
}
