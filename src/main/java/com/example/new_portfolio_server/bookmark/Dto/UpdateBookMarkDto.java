package com.example.new_portfolio_server.bookmark.Dto;

import com.example.new_portfolio_server.bookmark.BookMark;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookMarkDto {
    private Boolean enabled;
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
