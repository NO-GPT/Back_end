package com.example.new_portfolio_server.board.exception;

import lombok.Getter;

@Getter
public class CustomApplicationException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomApplicationException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
