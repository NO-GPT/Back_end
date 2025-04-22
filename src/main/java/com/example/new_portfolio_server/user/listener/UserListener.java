package com.example.new_portfolio_server.user.listener;

import com.example.new_portfolio_server.board.BoardService;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.user.entity.User;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class UserListener {
    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        UserListener.context = applicationContext;
    }

    @PostUpdate
    public void onUserChange(User user) {
        try {
            BoardService portfolioService = context.getBean(BoardService.class);
            for (Portfolio portfolio : user.getPortfolios()) {
                portfolioService.indexPortfolio(portfolio);
            }
        } catch (Exception e) {
            System.err.println("Failed to reindex portfolios for user: " + e.getMessage());
        }
    }
}
