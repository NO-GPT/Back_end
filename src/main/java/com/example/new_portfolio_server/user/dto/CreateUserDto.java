package com.example.new_portfolio_server.user.dto;

import com.example.new_portfolio_server.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Setter
@Getter
public class CreateUserDto {
    @NotNull(message = "아이디는 필수 입력 값입니다.")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 12, message = "아이디는 4~12자 입니다")
    private String username;

    @NotNull(message = "비밀번호는 필수 입력 값입니다.")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotNull(message = "이메일은 필수 입력 값입니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotNull(message = "이름은 필수 입력 값입니다.")
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "이름은 특수문자를 제외한 2~10자리여야 합니다.")
    private String fullName;

    @NotNull(message = "분야는 필수 입력 값입니다.")
    @NotBlank(message = "분야는 필수 입력 값입니다.")
    private String field;

    @NotNull(message = "소속은 필수 입력 값입니다.")
    @NotBlank(message = "소속은 필수 입력 값입니다.")
    private String group;

    @NotNull(message = "보유 기술은 필수 입력 값입니다.")
    @NotBlank(message = "보유 기술은 필수 입력 값입니다.")
    private String stack;

    @NotNull(message = "깃허브 아이디는 필수 입력 값입니다.")
    @NotBlank(message = "깃허브 아이디는 필수 입력 값입니다.")
    private String githubId;

    private String profile;

    public User toEntity(PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(this.username)
                .password(passwordEncoder.encode(this.password))
                .email(this.email)
                .fullName(this.fullName)
                .field(this.field)
                .group(this.group)
                .stack(this.stack)
                .githubId(this.githubId)
                .profile(this.profile)
                .build();
    }

}
