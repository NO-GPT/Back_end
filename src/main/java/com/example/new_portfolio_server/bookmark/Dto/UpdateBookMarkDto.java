package com.example.new_portfolio_server.bookmark.Dto;

import com.example.new_portfolio_server.bookmark.BookMark;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookMarkDto {
    @NotNull(message = "북마크 상태는 필수입니다.")
    private Boolean enabled; // 수정 시 null 허용하지 않음

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Long sort;

    // BookMark 엔티티에 적용하는 메서드 (업데이트용)
    public void applyTo(BookMark bookMark) {
        bookMark.setEnabled(this.enabled);
        bookMark.setSort(this.sort);
        // modified는 @PreUpdate에서 자동 설정
    }
}
