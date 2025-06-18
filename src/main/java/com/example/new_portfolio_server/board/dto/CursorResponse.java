package com.example.new_portfolio_server.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
public class CursorResponse {
    private Long nextLikeCount;
    private Long nextCursorId;
    private List<ResponseBoardDto> portfolios;
}
