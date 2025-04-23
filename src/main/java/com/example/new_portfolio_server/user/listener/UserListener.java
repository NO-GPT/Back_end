package com.example.new_portfolio_server.user.listener;

import com.example.new_portfolio_server.board.BoardService;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.user.entity.User;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/*
* 사용자 엔티티의 변경을 감지하여 관련 포트폴리오를 Typesense에 재인덱싱합니다.
* 사용자 정보가 변경되면 그 사용자의 포트폴리오도 업데이트되어야 합니다.
* */
@Component
public class UserListener {
    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        UserListener.context = applicationContext;
    }

    // 사용자 정보가 수정(@PostUpdate)되면 해당 사용자의 모든 포트폴리오를 재인덱싱
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
