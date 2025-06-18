package com.example.new_portfolio_server.board.repsoitory;

import com.example.new_portfolio_server.board.entity.Portfolio;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.sound.sampled.Port;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
    @Query("SELECT p FROM Portfolio p WHERE p.skills LIKE %:skill% ORDER BY SIZE(p.bookMarks) DESC")
    List<Portfolio> findBySkillOrderByBookMarksSizeDesc(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT p FROM Portfolio p ORDER BY SIZE(p.bookMarks) DESC")
    List<Portfolio> findAllOrderByBookMarksSizeDesc(Pageable pageable);
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

    List<Portfolio> findByUserId_Username(String username);

    // part 검색
    List<Portfolio> findByPartIn(List<String> parts);

    // group 검색
    List<Portfolio> findByUser_GroupIn(List<String> groups);

    // part + group 검색
    List<Portfolio> findByPartInAndUser_GroupIn(List<String> parts, List<String> groups);
}
