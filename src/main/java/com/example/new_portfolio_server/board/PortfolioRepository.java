package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.entity.Portfolio;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
    // 초기 조회 - 커서 없을 시, 북마크 개수 -> id 순으로 DESC 정렬
    @Query(value = """
        SELECT p.* FROM portfolio p
        LEFT JOIN book_mark bm ON p.id = bm.portfolio_id
        GROUP BY p.id
        ORDER BY COUNT(bm.id) DESC, p.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Portfolio> findInitialPortfolios(@Param("limit") int limit);


    @Query(value = """
        SELECT p.* FROM portfolio p
        LEFT JOIN book_mark bm ON p.id = bm.portfolio_id
        GROUP BY p.id
        HAVING COUNT(bm.id) < :cursorBookmarkCount
           OR (COUNT(bm.id) = :cursorBookmarkCount AND p.id < :cursorId)
        ORDER BY COUNT(bm.id) DESC, p.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Portfolio> findPortfolioByCursor(
            @Param("cursorBookmarkCount") long cursorBookmarkCount,
            @Param("cursorId") long cursorId,
            @Param("limit") int limit
    );
}
