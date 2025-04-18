package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
}
