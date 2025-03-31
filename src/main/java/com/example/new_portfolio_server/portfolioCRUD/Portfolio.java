package com.example.new_portfolio_server.portfolioCRUD;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "portfolio")
@NoArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 식별자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @CreatedDate
    @Column(name = "date_time")
    private LocalDateTime dateTime; // 게시 날짜

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @CreatedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime; // 수정날짜

    @Column(name = "project_introduce", length = 60)
    private String projectIntroduce; // 프로젝트 소개

    @Column(name = "project_part", length = 500)
    private String projectPart; // 프로젝트 맡은 역할/기능

    @Column(name = "project_content", length = 400)
    private String projectContent; // 배운 점 / 문제 해결

    @Column(name = "project_files_path", length = 200)
    private String projectFilesPath; // 프로젝트 파일 경로(시각적 자료)

    @Column(name = "project_links", length = 200)
    private String projectLinks; // 프로젝트 참고 링크(github, notion 등)

    @Column(name = "programing_skills", length = 300)
    private String programingSkills; // 사용한 스킬(사용한 툴, 언어 등)

    @Column(name = "userid")
    private String userId; // 유저 아이디(게시글 식별)
}

