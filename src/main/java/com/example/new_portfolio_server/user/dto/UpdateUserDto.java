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
    @Size(min = 4, max = 12, message = "아이디는 4~12자 입니다")
    private String username;

    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "이름은 특수문자를 제외한 2~10자리여야 합니다.")
    private String fullName;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String tel;

    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하이어야 합니다.")
    @Schema(description = "나이", example = "20")
    @Min(0)
    @Max(150)
    private int age;

    private String field;

    private String group;

    private String stack;

    private String githubId;

    private String profile;

    public void updateEntity(User user, PasswordEncoder passwordEncoder) {
        if (this.username != null) user.setUsername(this.username);
        if (this.password != null) user.setPassword(passwordEncoder.encode(this.password));
        if (this.email != null) user.setEmail(this.email);
        if (this.fullName != null) user.setFullName(this.fullName);
        if (this.tel != null) user.setTel(this.tel);
        if (this.age != 0) user.setAge(this.age); // 0은 기본값으로 간주
        if (this.field != null) user.setField(this.field);
        if (this.group != null) user.setGroup(this.group);
        if (this.stack != null) user.setStack(this.stack);
        if (this.githubId != null) user.setGithubId(this.githubId);
        if (this.profile != null) user.setProfile(this.profile);
    }
}
