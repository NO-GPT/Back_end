package com.example.new_portfolio_server.portfolioCRUD;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{ // 기본 CRUD 메서드 제공

}
