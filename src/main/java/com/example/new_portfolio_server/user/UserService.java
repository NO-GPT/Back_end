package com.example.new_portfolio_server.user;
import com.example.new_portfolio_server.common.exception.DuplicateResourceException;
import com.example.new_portfolio_server.user.dto.CreateUserDto;
import com.example.new_portfolio_server.user.entity.User;
import lombok.RequiredArgsConstructor;
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
        User user = dto.toEntity(passwordEncoder);
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
