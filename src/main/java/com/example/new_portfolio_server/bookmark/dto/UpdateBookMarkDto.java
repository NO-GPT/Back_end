package com.example.new_portfolio_server.bookmark.dto;

import com.example.new_portfolio_server.bookmark.entity.BookMark;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "북마크 수정 요청 DTO")
public class UpdateBookMarkDto {
    @Schema(description = "북마크 상태", example = "false")
    private Boolean enabled;

    @Schema(description = "정렬 순서", example = "2")
    private Long sort;

    // BookMark 엔티티에 적용하는 메서드 (업데이트용)
    public void applyTo(BookMark bookMark) {
        if (this.enabled != null) {
            bookMark.setEnabled(this.enabled);
        }
        if (this.sort != null) {
            bookMark.setSort(this.sort);
        }
        // modified는 @PreUpdate에서 자동 설정
    }
}
