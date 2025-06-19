package com.example.new_portfolio_server.comments;

import com.example.new_portfolio_server.comments.dto.CommentEditRequestDto;
import com.example.new_portfolio_server.comments.dto.CommentRequestDto;
import com.example.new_portfolio_server.comments.dto.CommentResponseDto;
import com.example.new_portfolio_server.comments.entity.Comments;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
@Tag(name = "Comment API", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    // 댓글 게시
    @PostMapping("/create")
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody @Valid CommentRequestDto commentRequestDto){
        return ResponseEntity.ok(commentService.createComment(commentRequestDto));
    }

//    // 댓글 전체 조회
//    @GetMapping
//    public ResponseEntity<List<CommentResponseDto>> getCommentAll() {
//        return ResponseEntity.ok(commentService.getCommentAll());
//    }

    @GetMapping("/list/{portfolioId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long portfolioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int limit
    ){
        List<CommentResponseDto> result = commentService.getAllCommentSortedByDate(portfolioId, cursorCreatedAt, cursorId, limit);
        return ResponseEntity.ok(result);
    }

    // 댓글 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDto> getComment(@PathVariable Long id){
        CommentResponseDto commentResponseDto = commentService.getComment(id);
        return ResponseEntity.ok(commentResponseDto);
    }

    // 포트폴리오에 달린 댓글 조회
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentByPortfolioId(@PathVariable Long portfolioId){
        List<CommentResponseDto> commentResponseDtos = commentService.getCommentByPortfolio(portfolioId);
        return ResponseEntity.ok(commentResponseDtos);
    }

    // 댓글 수정
    @PatchMapping("/update/{id}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentEditRequestDto commentEditRequestDto
            ){
        CommentResponseDto commentResponseDto = commentService.updateComment(id, commentEditRequestDto);
        return ResponseEntity.ok(commentResponseDto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id){
        commentService.deleteComment(id);
        return ResponseEntity.ok("댓글 삭제 성공");
    }
}
