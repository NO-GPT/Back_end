package com.example.new_portfolio_server.board.exception;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(String message)
    {
        super(message);
    }
}
