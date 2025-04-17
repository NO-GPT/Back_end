package com.example.new_portfolio_server.user;
import com.example.new_portfolio_server.common.Exception.DuplicateResourceException;
import com.example.new_portfolio_server.user.Dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Long createUser(CreateUserDto dto) {
        checkUserEmailAndUsername(dto.getUsername(), dto.getEmail(), null);

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .field(dto.getField())
                .group(dto.getGroup())
                .stack(dto.getStack())
                .githubId(dto.getGithubId())
                .profile(dto.getProfile())
                .build();

        userRepository.save(user);

        return user.getId();
    }

    public void checkUserEmailAndUsername(String username, String email, Long userId) {
        if (userId == null) { // 새 사용자 생성 시
            if (userRepository.existsByUsername(username)) {
                throw new DuplicateResourceException("이미 사용중인 아이디 입니다");
            }
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("이미 사용중인 이메일 입니다");
            }
        } else { // 기존 사용자 업데이트 시
            if (username != null && userRepository.existsByUsernameAndIdNot(username, userId)) {
                throw new DuplicateResourceException("이미 사용중인 아이디 입니다");
            }
            if (email != null && userRepository.existsByEmailAndIdNot(email, userId)) {
                throw new DuplicateResourceException("이미 사용중인 이메일 입니다");
            }
        }
    }
}
