package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "포트폴리오 셍성 요청 DTO")
public class BoardDto {
    @NotBlank(message = "프로젝트 소개는 필수입니다.")
    @Schema(description = "프로젝트 소개", example = "에어팟의 모션 감지 센서를 활용한 거북목 예방 솔루션")
    private String introduce;

    @NotNull(message = "맡은 역할/기능은 필수입니다.")
    @Schema(description = "맡은 기능 혹은 역할", example = "백엔드/인프라")
    private String part;

    @NotNull(message = "배운 점/문제 해결은 필수입니다.")
    @Schema(description = "트러블 슈팅", example = "해당 프로젝트에선 ~~한 문제가 있었고, ~~이렇게 해결했습니다. 이 과정에서 ~~를 배웠습니다.")
    private String content;

    @Size(max = 200, message = "링크는 최대 200자까지 입력 가능합니다.")
    @Schema(description = "프로젝트 베포 링크 혹은 깃허브 링크 (혹은 노션)", example = "https://github.com/example")
    private String links;

    @NotNull(message = "사용한 스킬은 필수입니다.")
    @Size(max = 300, message = "사용한 스킬은 최대 300자까지 입력 가능합니다.")
    @Schema(description = "기술 스택", example = "NestJS, VueJS, TypeORM, PostgresSQL, Docker, Redis ...")
    private String skills;

    @Schema(description = "첨부할 프로젝트 사진")
    private List<MultipartFile> files;

    @NotNull(message = "작성자 ID는 필수입니다.")
    @Schema(description = "작성자 아이디", example = "1")
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
