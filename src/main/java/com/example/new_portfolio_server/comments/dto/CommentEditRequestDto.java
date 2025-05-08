package com.example.new_portfolio_server.comments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEditRequestDto {

    @NotBlank(message = "댓글 내용을 작성해주세요.")
    private String body;
}
