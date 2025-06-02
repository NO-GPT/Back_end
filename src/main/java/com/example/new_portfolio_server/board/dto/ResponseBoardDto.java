package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResponseBoardDto {
    private Long id; // 포트폴리오 ID
    private String introduce; // 프로젝트 소개
    private String part; // 맡은 역할/기능
    private String content; // 배운 점/문제 해결
    private String links; // 참고 링크
    private String skills; // 사용한 스킬

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createDate; // 생성 날짜

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime updateDate; // 수정 날짜

    private Long userId; // 작성자 ID

    private Long bookmarkCount; // 북마크 개수

    public static ResponseBoardDto fromEntity(Portfolio portfolio) {
        return new ResponseBoardDto(
                portfolio.getId(),
                portfolio.getIntroduce(),
                portfolio.getPart(),
                portfolio.getContent(),
                portfolio.getLinks(),
                portfolio.getSkills(),
                portfolio.getCreateDate(),
                portfolio.getUpdateDate(),
                portfolio.getUserId().getId(), // 작성자 ID
                (long) portfolio.getBookMarks().size() // 해당 포트폴리오에 연결된 북마크 수
        );
    }
}