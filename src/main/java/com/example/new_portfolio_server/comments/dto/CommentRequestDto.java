package com.example.new_portfolio_server.comments.dto;

import com.example.new_portfolio_server.comments.entity.Comments;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentRequestDto {
    @NotNull(message = "댓글 내용을 작성해주세요.")
    @NotBlank(message = "댓글 내용을 작성해주세요.")
    private String body;

    private Long portfolioId;

    private Long userId;

    public Comments toEntity(){
        return Comments.builder()
                .body(this.body)
                .build();
    }
}
