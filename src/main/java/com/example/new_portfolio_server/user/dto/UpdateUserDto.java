package com.example.new_portfolio_server.user.dto;

import com.example.new_portfolio_server.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Setter
@Getter
public class UpdateUserDto {
    @NotNull(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 12, message = "아이디는 4~12자 입니다")
    private String username;

    @NotNull(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotNull(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotNull(message = "이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "이름은 특수문자를 제외한 2~10자리여야 합니다.")
    private String fullName;

    @NotNull(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String tel;

    @NotNull(message = "나이는 필수 입력 값입니다.")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하이어야 합니다.")
    @Schema(description = "나이", example = "20")
    @Min(0)
    @Max(150)
    private int age;

    @NotNull(message = "분야는 필수 입력 값입니다.")
    private String field;

    @NotNull(message = "소속은 필수 입력 값입니다.")
    private String group;

    @NotNull(message = "보유 기술은 필수 입력 값입니다.")
    private String stack;

    @NotNull(message = "깃허브 아이디는 필수 입력 값입니다.")
    private String githubId;

    private String profile;

    public void toEntity(PasswordEncoder passwordEncoder) {
        User.builder()
                .username(this.username)
                .password(passwordEncoder.encode(this.password))
                .email(this.email)
                .fullName(this.fullName)
                .tel(this.tel)
                .age(this.age)
                .field(this.field)
                .group(this.group)
                .stack(this.stack)
                .githubId(this.githubId)
                .profile(this.profile)
                .build();
    }
}
