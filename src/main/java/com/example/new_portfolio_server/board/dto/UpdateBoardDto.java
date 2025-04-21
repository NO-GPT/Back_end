package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardDto {
    private String introduce;
    private String part;
    private String content;
    private String links;
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
