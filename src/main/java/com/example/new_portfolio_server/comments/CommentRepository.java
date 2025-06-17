package com.example.new_portfolio_server.comments;

import com.example.new_portfolio_server.comments.entity.Comments;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.xml.stream.events.Comment;
import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {
    List<Comments> findAllByPortfolioId(Long portfolioId);

    // 커서 없을 때
    @Query(value = """
            SELECT * FROM comments
            WHERE portfolio_id = :portfolioId
            ORDER BY create_date DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Comments> findInitialComments(
            @Param("portfolioId") Long portfolioId,
            @Param("limit") int limit
    );

    // 커서 있을 때
    @Query(value = """
            SELECT * FROM comments
            WHERE portfolio_id = :portfolioId
            AND(create_date < :cursorCreatedAt
                OR(create_date = :cursorCreatedAt AND id < :cursorId))
            ORDER BY create_date DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Comments> findByCursor(
            @Param("portfolioId") Long portfolioId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );
}
