package com.example.new_portfolio_server.board.likes.repository;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.board.likes.entity.Like;
import com.example.new_portfolio_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndPortfolio(User user, Portfolio portfolio);

    void deleteByUserAndPortfolio(User user, Portfolio portfolio);

    List<Like> findByUserId(Long userId);

}
