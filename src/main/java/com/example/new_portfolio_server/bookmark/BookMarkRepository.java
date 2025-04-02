package com.example.new_portfolio_server.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    List<BookMark> findByUserId(Long userId);  // user_id로 조회
}
