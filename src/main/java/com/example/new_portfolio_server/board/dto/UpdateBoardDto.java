package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "포트폴리오 수정 요청 DTO")
public class UpdateBoardDto {
    @Schema(description = "프로젝트 소개", example = "해당 프로젝트는 ...")
    private String introduce;

    @Schema(description = "담당한 역할/기능", example = "백엔드/인프라")
    private String part;

    @Schema(description = "트러블 슈팅", example = "여러웠던점, 해결방법, 배운점")
    private String content;

    @Schema(description = "깃헙 노션 베포링크 등등", example = "http://example.com")
    private String links;

    @Schema(description = "사용한 기술 스택", example = "NodeJS, Go, MySQL, Docker-Compose, React ...")
    private String skills;

    // Portfolio 엔티티에 값 적용
    public void applyTo(Portfolio portfolio) {
        if (this.introduce != null && !this.introduce.trim().isEmpty()) {
            portfolio.setIntroduce(this.introduce);
        }
        if (this.part != null && !this.part.trim().isEmpty()) {
            portfolio.setPart(this.part);
        }
        if (this.content != null && !this.content.trim().isEmpty()) {
            portfolio.setContent(this.content);
        }
        if (this.links != null && !this.links.trim().isEmpty()) {
            portfolio.setLinks(this.links);
        }
        if (this.skills != null && !this.skills.trim().isEmpty()) {
            portfolio.setSkills(this.skills);
        }
    }
}
