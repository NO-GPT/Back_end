package com.example.new_portfolio_server.bookmark.Dto;

import com.example.new_portfolio_server.bookmark.BookMark;
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

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Long sort;

    @Builder.Default
    private boolean enabled = true; // 기본값: 북마크 활성화

    // 나중에 Board 엔티티가 추가되면 사용
    // @NotNull(message = "게시물 ID는 필수입니다.")
    // private Long boardId;

    // BookMark 엔티티로 변환하는 메서드
    public BookMark toEntity() {
        return BookMark.builder()
                .userId(this.userId)
                .sort(this.sort)
                .enabled(this.enabled)
                // .board(board) // Board 엔티티가 완성되면 추가
                .build();
    }
}
