package com.example.new_portfolio_server.bookmark.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseBookmarkDto {
    private Long id; // 북마크 ID
    private boolean enabled; // 북마크 상태
    private Long sort; // 정렬 순서
    private Portfolio portfolio; // 포트폴리오 정보
}
