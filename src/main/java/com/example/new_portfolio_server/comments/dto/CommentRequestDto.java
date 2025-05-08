package com.example.new_portfolio_server.comments.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.comments.entity.Comments;
import com.example.new_portfolio_server.user.entity.User;
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

    private String username;

    public Comments toEntity(Portfolio portfolio, User user){
        return Comments.builder()
                .body(this.body)
                .portfolio(portfolio)
                .user(user)
                .build();
    }
}
