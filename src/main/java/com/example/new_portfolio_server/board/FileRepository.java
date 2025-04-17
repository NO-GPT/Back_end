package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByPortfolioId(Long portfolio);
}
