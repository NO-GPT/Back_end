package com.example.new_portfolio_server.bookmark;

import com.example.new_portfolio_server.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Entity
@Builder
@Getter
@Setter
@Table(name = "book_mark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 포폴 기능이 완성되면
//    @Comment("게시물 id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "board_id")
//    @NotNull
//    private Board board;

    @Comment("유저 id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore  // User 정보 숨김
    @NotNull
    private User user;

    @Comment("북마크 상태")
    @Column()
    @NotNull
    private boolean enabled; //  true 즐겨찾기, false 즐겨찾기 취소

    @Comment("정렬")
    @Column()
    @NotNull
    private Long sort;

    @Comment("북마크 생성 날짜")
    @Column()
    @NotNull
    private ZonedDateTime created;

    @Comment("북마크 수정 날짜")
    @Column()
    private ZonedDateTime modified;

    @PrePersist
    public void prePersist() {
        if (created == null) {
            created = ZonedDateTime.now();
        }
        if (modified == null) {
            modified = ZonedDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (modified == null) {
            modified = ZonedDateTime.now();
        }
    }

//    public BoardFavorite(Board board, Long memberId) {
//        this.board = board;
//        this.memberId = memberId;
//        this.enabled = true;
//    }
}
