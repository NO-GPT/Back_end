package com.example.new_portfolio_server.bookmark.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.bookmark.entity.BookMark;
import com.example.new_portfolio_server.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookMarkDto {
    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "포트폴리오 ID는 필수입니다.")
    private Long portfolioId;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Long sort;

    @Builder.Default
    private boolean enabled = true; // 기본값: 북마크 활성화

    // BookMark 엔티티로 변환하는 메서드
    public BookMark toEntity(User user, Portfolio portfolio) {
        return BookMark.builder()
                .user(user)
                .sort(this.sort)
                .enabled(this.enabled)
                .portfolio(portfolio)
                .build();
    }
}
