package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.entity.Portfolio;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
    @Query("SELECT p FROM Portfolio p WHERE p.skills LIKE %:skill% ORDER BY SIZE(p.bookMarks) DESC")
    List<Portfolio> findBySkillOrderByBookMarksSizeDesc(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT p FROM Portfolio p ORDER BY SIZE(p.bookMarks) DESC")
    List<Portfolio> findAllOrderByBookMarksSizeDesc(Pageable pageable);
}
