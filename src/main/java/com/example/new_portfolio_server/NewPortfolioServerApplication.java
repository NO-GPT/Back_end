package com.example.new_portfolio_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.example.new_portfolio_server.board.entity",
        "com.example.new_portfolio_server.user.entity",
        "com.example.new_portfolio_server.bookmark.entity",
        "com.example.new_portfolio_server.comments.entity",
        "com.example.new_portfolio_server.board.likes.entity"
})// 자동으로 값을 저장 (게시 날짜, 수정 날짜)
@EnableJpaRepositories(basePackages = {
        "com.example.new_portfolio_server.board",
        "com.example.new_portfolio_server.user",
        "com.example.new_portfolio_server.bookmark",
        "com.example.new_portfolio_server.comments"
})
@EnableJpaAuditing
public class NewPortfolioServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewPortfolioServerApplication.class, args);
    }
}
