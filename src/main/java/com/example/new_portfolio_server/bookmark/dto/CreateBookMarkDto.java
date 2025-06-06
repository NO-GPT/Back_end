package com.example.new_portfolio_server.bookmark.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.bookmark.entity.BookMark;
import com.example.new_portfolio_server.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "북마크 생성 요청 DTO")
public class CreateBookMarkDto {
    @NotNull(message = "유저 ID는 필수입니다.")
    @Schema(description = "북마크 생성자 아이디", example = "1")
    private Long userId;

    @NotNull(message = "포트폴리오 ID는 필수입니다.")
    @Schema(description = "북마크 하려는 포폴 아이디", example = "1")
    private Long portfolioId;

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬순서", example = "1")
    private Long sort;

    @Builder.Default
    @Schema(description = "북마크 활성화", example = "true")
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
