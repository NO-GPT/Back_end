package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseBoardDto {
    private Long id; // 포트폴리오 ID
    private String introduce; // 프로젝트 소개
    private String part; // 맡은 역할/기능
    private String content; // 배운 점/문제 해결
    private String links; // 참고 링크
    private String skills; // 사용한 스킬
    private Long userId; // 작성자 ID

    public static ResponseBoardDto fromEntity(Portfolio portfolio) {
        return new ResponseBoardDto(
                portfolio.getId(),
                portfolio.getIntroduce(),
                portfolio.getPart(),
                portfolio.getContent(),
                portfolio.getLinks(),
                portfolio.getSkills(),
                portfolio.getUserId().getId() // 작성자 ID
        );
    }
}