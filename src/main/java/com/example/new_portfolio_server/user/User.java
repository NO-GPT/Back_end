package com.example.new_portfolio_server.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA에서 기본 생성자 필요, 외부 접근 차단
@AllArgsConstructor                                 // 모든 필드를 포함한 생성자
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, unique = false)       // 실명
    private String fullName;

    @Column(name = "field", nullable = false, unique = false)           // 분야 (예 : 백엔드 / 프론트 / devops
    private String field;

    @Column(name = "user_group", nullable = false, unique = false)      // 소속
    private String group;

    @Column(name = "stack", nullable = false, unique = false)           // 보유 기술
    private String stack;

    @Column(name = "github_id", nullable = true, unique = false)        // 깃허브 ID
    private String githubId;

    @Column(name = "profile", nullable = true, unique = false)          // 프로필사진
    private String profile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
