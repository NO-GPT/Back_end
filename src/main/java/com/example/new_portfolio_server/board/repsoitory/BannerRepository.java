package com.example.new_portfolio_server.board.repsoitory;

import com.example.new_portfolio_server.board.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByPortfolioId(Long PortfolioId);
}
