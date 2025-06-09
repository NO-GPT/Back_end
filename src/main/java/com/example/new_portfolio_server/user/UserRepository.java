package com.example.new_portfolio_server.user;

import com.example.new_portfolio_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByTel(String tel);

    // 특정 사용자 ID를 제외하고 username 중복 검사
    boolean existsByUsernameAndIdNot(String username, Long id);

    // 특정 사용자 ID를 제외하고 email 중복 검사
    boolean existsByEmailAndIdNot(String email, Long id);

    // 특정 사용자 ID를 제외하고 tel 중복 검사
    boolean existsByTelAndIdNot(String tel, Long id);
}
