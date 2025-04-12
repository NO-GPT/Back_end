package com.example.new_portfolio_server.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String introduce;
    private String part;
    private String content;
    private String links;
    private String skills;
    private List<MultipartFile> files;
}
