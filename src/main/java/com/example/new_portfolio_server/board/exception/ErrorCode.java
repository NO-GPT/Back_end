package com.example.new_portfolio_server.board.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_EXISTS_FILE(HttpStatus.BAD_REQUEST, "파일이 존재하지 않습니다."),
    NOT_EXISTS_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "파일 확장자가 존재하지 않습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다.");
    private final HttpStatus status;
    private final String message;
}
