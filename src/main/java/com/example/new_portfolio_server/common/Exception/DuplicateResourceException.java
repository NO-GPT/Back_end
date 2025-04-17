package com.example.new_portfolio_server.common.Exception;

//  중복 에러를 메시지로 받기위해
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}