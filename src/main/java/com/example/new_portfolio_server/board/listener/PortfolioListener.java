package com.example.new_portfolio_server.board.listener;

import com.example.new_portfolio_server.board.BoardService;
import com.example.new_portfolio_server.board.entity.Portfolio;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortfolioListener {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioListener.class);

    private final @Lazy BoardService boardService;

    @PostPersist
    @PostUpdate
    public void onPortfolioChange(Portfolio portfolio) {
        try {
            boardService.indexPortfolio(portfolio);
            logger.debug("Portfolio indexed successfully: ID={}", portfolio.getId());
        } catch (Exception e) {
            logger.error("Failed to index portfolio ID={}: {}", portfolio.getId(), e.getMessage(), e);
        }
    }

    @PostRemove
    public void onPortfolioRemove(Portfolio portfolio) {
        try {
            String portfolioId = String.valueOf(portfolio.getId());
            boardService.getTypesenseClient()
                    .collections(BoardService.PORTFOLIO_COLLECTION)
                    .documents(portfolioId)
                    .delete();
            boardService.getTypesenseClient()
                    .collections(BoardService.PORTFOLIO_WITH_USERS_COLLECTION)
                    .documents(portfolioId)
                    .delete();
            logger.debug("Portfolio deleted from Typesense successfully: ID={}", portfolioId);
        } catch (Exception e) {
            logger.error("Failed to delete portfolio ID={} from Typesense: {}", portfolio.getId(), e.getMessage(), e);
        }
    }
}