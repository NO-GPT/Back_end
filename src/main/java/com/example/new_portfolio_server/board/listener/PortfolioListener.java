package com.example.new_portfolio_server.board.listener;

import com.example.new_portfolio_server.board.BoardService;
import com.example.new_portfolio_server.board.entity.Portfolio;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 포트폴리오 엔티티의 변경(추가, 수정, 삭제)을 감지하여 Typesense 인덱스를 자동으로 업데이트
@Component
@RequiredArgsConstructor
public class PortfolioListener {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioListener.class);

    private final BoardService boardService;

    // 포트폴리오가 추가(@PostPersist) 또는 수정(@PostUpdate)되면 Typesense에 인덱싱
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

    // 포트폴리오가 삭제(@PostRemove)되면 Typesense에서 제거
    @PostRemove
    public void onPortfolioRemove(Portfolio portfolio) {
        try {
            String portfolioId = String.valueOf(portfolio.getId());
            boardService.getTypesenseClient()
                    .collections(BoardService.PORTFOLIO_COLLECTION)
                    .documents(portfolioId)
                    .delete();
            logger.debug("Portfolio deleted from Typesense successfully: ID={}", portfolioId);
        } catch (Exception e) {
            logger.error("Failed to delete portfolio ID={} from Typesense: {}", portfolio.getId(), e.getMessage(), e);
        }
    }
}