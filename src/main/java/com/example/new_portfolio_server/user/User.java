package com.example.new_portfolio_server.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    @Comment("유저 아이디")
    @Column(nullable = false, unique = true)
    private String username;

    @Comment("유저 이메일")
    @Column(nullable = false, unique = true)
    private String email;

    @Comment("유저 비밀번호")
    @Column(nullable = false)
    private String password;

    @Comment("유저 실제 이름")
    @Column(name = "full_name", nullable = false, unique = false)       // 실명
    private String fullName;

    @Comment("유저의 직업 분야")
    @Column(name = "field", nullable = false, unique = false)           // 분야 (예 : 백엔드 / 프론트 / devops
    private String field;

    @Comment("유저 소속")
    @Column(name = "user_group", nullable = false, unique = false)      // 소속
    private String group;

    @Comment("유저의 사용 스택")
    @Column(name = "stack", nullable = false, unique = false)           // 보유 기술
    private String stack;

    @Comment("유저 깃허브ID")
    @Column(name = "github_id", nullable = true, unique = false)        // 깃허브 ID
    private String githubId;

    @Comment("유저 프로필 사진")
    @Column(name = "profile", nullable = true, unique = false)          // 프로필사진
    private String profile;

    @Comment("계정 생성 시간")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Comment("계정 업데이트 시간")
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
