package com.example.new_portfolio_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // 자동으로 값을 저장 (게시 날짜, 수정 날짜)
public class NewPortfolioServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewPortfolioServerApplication.class, args);
    }

}
