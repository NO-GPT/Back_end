package com.example.new_portfolio_server.comments.entity;

import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "body", length = 1000, nullable = false)
    private String body;

    @Column(name = "create_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "edit_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    @LastModifiedDate
    private LocalDateTime editDate;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
