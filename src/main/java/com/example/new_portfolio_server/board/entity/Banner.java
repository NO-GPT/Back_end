package com.example.new_portfolio_server.board.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "banner_files")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 파일 식별

    @Column(name = "banner_name")
    private String bannerName; // 파일 이름

    @Column(name = "contentType")
    private String contentType; // 파일 확장자

    @Column(name = "banner_size")
    private Long size; // 파일 사이즈

    @Column(name = "banner_url")
    private String bannerUrl; // S3 URL 저장

    @Column(name = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio; // 포트폴리오 id
}
