package com.example.new_portfolio_server.board.likes.dto;


import com.example.new_portfolio_server.board.likes.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponse {
    private Long id;
    private Long userId;
    private Long portfolioId;
    private String message;

    public static LikeResponse fromEntity(Like like, String message){
        return new LikeResponse(
                like.getId(),
                like.getUser().getId(),
                like.getPortfolio().getId(),
                message
        );
    }
}
