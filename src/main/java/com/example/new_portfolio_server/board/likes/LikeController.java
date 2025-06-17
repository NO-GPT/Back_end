package com.example.new_portfolio_server.board.likes;

import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.likes.dto.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikeController {
    private final LikeService likeService;

    // 좋아요 생성/삭제
    @PostMapping("/{portfolioId}")
    public ResponseEntity<LikeResponse> likeResponseResponseEntity(
            @PathVariable Long portfolioId,
            @RequestParam Long userId){
        LikeResponse response = likeService.goodPortfolio(portfolioId, userId);
        return ResponseEntity.ok(response);
    }

    // 유저가 좋아요를 표시한 포트폴리오 정보 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResponseBoardDto>> getLikesPortfolioByUserId(@PathVariable Long userId){
        List<ResponseBoardDto> response = likeService.getLikeUser(userId);
        return ResponseEntity.ok(response);
    }
}
