package com.example.new_portfolio_server.board.entity;

import com.example.new_portfolio_server.user.User;
import com.example.new_portfolio_server.bookmark.BookMark;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "portfolio")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 식별자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @CreatedDate
    @Column(name = "createDate", nullable = false)
    private LocalDateTime createDate; // 게시 날짜

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @LastModifiedDate
    @Column(name = "updateDate")
    private LocalDateTime updateDate; // 수정날짜

    @Column(name = "introduce", length = 60, nullable = false)
    private String introduce; // 프로젝트 소개

    @Column(name = "part", length = 500, nullable = false)
    private String part; // 프로젝트 맡은 역할/기능

    @Column(name = "content", length = 400, nullable = false)
    private String content; // 배운 점 / 문제 해결

    @Column(name = "links", length = 200)
    private String links; // 프로젝트 참고 링크(github, notion 등)

    @Column(name = "skills", length = 300, nullable = false)
    private String skills; // 사용한 스킬(사용한 툴, 언어 등)

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true) // files PK
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookMark> bookMarks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래 키 명시
    @JsonIgnore
    private User userId;
}

