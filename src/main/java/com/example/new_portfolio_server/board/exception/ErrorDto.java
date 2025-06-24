package com.example.new_portfolio_server.board.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDto {
//    private String code;
    private String message;
    private String detail;
}
