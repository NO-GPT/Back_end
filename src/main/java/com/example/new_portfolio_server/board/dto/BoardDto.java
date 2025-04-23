package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {
    @NotNull(message = "프로젝트 소개는 필수입니다.")
    private String introduce;

    @NotNull(message = "맡은 역할/기능은 필수입니다.")
    private String part;

    @NotNull(message = "배운 점/문제 해결은 필수입니다.")
    private String content;

    @Size(max = 200, message = "링크는 최대 200자까지 입력 가능합니다.")
    private String links;

    @NotNull(message = "사용한 스킬은 필수입니다.")
    @Size(max = 300, message = "사용한 스킬은 최대 300자까지 입력 가능합니다.")
    private String skills;

    private List<MultipartFile> files;

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId; // 유저 ID 추가

    public Portfolio toEntity() {
        return Portfolio.builder()
                .introduce(this.introduce)
                .part(this.part)
                .content(this.content)
                .links(this.links)
                .skills(this.skills)
                .build();
    }
}
