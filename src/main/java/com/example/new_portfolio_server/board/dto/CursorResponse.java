package com.example.new_portfolio_server.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorResponse<T> {
    private List<T> content;
    private Long nextCursorId;
    private Long nextCursorBookmarkCount;
    private boolean hasNext;
}
