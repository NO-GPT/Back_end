package com.example.new_portfolio_server.bookmark.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "북마크 응답 DTO")
public class ResponseBookmarkDto {
    @Schema(description = "북마크 아이디", example = "1")
    private Long id;            // 북마크 ID

    @Schema(description = "북마크 상태", example = "true")
    private boolean enabled;    // 북마크 상태

    @Schema(description = "정렬 순서", example = "1")
    private Long sort;          // 정렬 순서

    @Schema(description = "북마크할 포폴 아이디", example = "1")
    private Long portfolioId;   // 포트폴리오 정보

    @Schema(description = "북마크 하는 사용자 아아디", example = "1")
    private Long userId;        // 작성자 ID
}
