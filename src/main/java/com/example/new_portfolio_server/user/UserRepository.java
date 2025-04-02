package com.example.new_portfolio_server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long userId);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // 특정 사용자 ID를 제외하고 username 중복 검사
    boolean existsByUsernameAndIdNot(String username, Long id);

    // 특정 사용자 ID를 제외하고 email 중복 검사
    boolean existsByEmailAndIdNot(String email, Long id);
}
