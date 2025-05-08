package com.example.new_portfolio_server.comments;

import com.example.new_portfolio_server.comments.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {
    List<Comments> findAllByPortfolioId(Long portfolioId);
}
