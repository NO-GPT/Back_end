package com.example.new_portfolio_server.comments.dto;

import com.example.new_portfolio_server.comments.entity.Comments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String body;
    private LocalDateTime createDate;
    private LocalDateTime editDate;

    private Long portfolio;

    private Long userId;
    private String username;

    public static CommentResponseDto fromEntity(Comments comments){
        return new CommentResponseDto(
                comments.getId(),
                comments.getBody(),
                comments.getCreateDate(),
                comments.getEditDate(),
                comments.getPortfolio().getId(),
                comments.getUser().getId(),
                comments.getUser().getUsername()
        );
    }
}
