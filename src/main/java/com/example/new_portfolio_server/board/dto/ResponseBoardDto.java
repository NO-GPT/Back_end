package com.example.new_portfolio_server.board.dto;

import com.example.new_portfolio_server.board.entity.Banner;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.bookmark.entity.BookMark;
import com.example.new_portfolio_server.comments.entity.Comments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "포트폴리오 응답 DTO")
public class ResponseBoardDto {
    @Schema(description = "포트폴리오 아이디", example = "1")
    private Long id; // 포트폴리오 ID

    @Schema(description = "프로젝트 소개", example = "해당 프로젝트는 ...")
    private String introduce; // 프로젝트 소개

    @Schema(description = "담당한 역할/기능", example = "백엔드/인프라")
    private String part; // 맡은 역할/기능

    @Schema(description = "트러블 슈팅", example = "문제상황, 해결과정, 배운점")
    private String content; // 배운 점/문제 해결

    @Schema(description = "참고링크(베포링크,노션,깃헙 등)", example ="https://github.com/example")
    private String links; // 참고 링크

    @Schema(description = "사용한 기술 스택", example = "SpringBoot, JPA, Redis, AWS, React ...")
    private String skills; // 사용한 스킬

    @Schema(description = "배너 사진")
    private List<Banner> banner;

    @Schema(description = "프로젝트 관련 사진")
    private List<File> files; // 파일 정보

    @Schema(description = "게시 날짜")
    private LocalDateTime createDate; // 게시 날짜

    @Schema(description = "수정 날짜")
    private LocalDateTime updateDate; // 수정 날짜

    @Schema(description = "작성자 아이디", example = "1")
    private Long userId; // 작성자 ID

    private String username; // 유저 닉네임

    private String userGroup; // 소속

    @Schema(description = "좋아요 개수", example = "0")
    private Long likeCount;

    @Schema(description = "해당 게시글에 달린 북마크 리스트")
    private List<BookMark> bookMarks; // 북마크 목록

    @Schema(description = "북마크 갯수", example = "4")
    private Long bookmarkCount; // 북마크 개수

    @Schema(description = "해당 게시글에 달린 댓글 리스트")
    private List<Comments> comments;


    public static ResponseBoardDto fromEntity(Portfolio portfolio) {
        return new ResponseBoardDto(
                portfolio.getId(),
                portfolio.getIntroduce(),
                portfolio.getPart(),
                portfolio.getContent(),
                portfolio.getLinks(),
                portfolio.getSkills(),
                portfolio.getBanner_file(),
                portfolio.getFiles(),                      // File 리스트
                portfolio.getCreateDate(),
                portfolio.getUpdateDate(),
                portfolio.getUser().getId(),
                portfolio.getUser().getUsername(),
                portfolio.getUser().getGroup(),
                portfolio.getLikeCount(),
                portfolio.getBookMarks(),                 // BookMark 리스트
                (long) portfolio.getBookMarks().size(),   // 북마크 수
                portfolio.getComments()                   // 댓글 리스트
        );
    }
}