package com.example.new_portfolio_server.bookmark;

import com.example.new_portfolio_server.bookmark.dto.CreateBookMarkDto;
import com.example.new_portfolio_server.bookmark.dto.ResponseBookmarkDto;
import com.example.new_portfolio_server.bookmark.dto.UpdateBookMarkDto;
import com.example.new_portfolio_server.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmark")
@Tag(name = "Book_Mark API", description = "북마크 관련 API")
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<ResponseBookmarkDto>>> getBookMarks(
            @PathVariable Long userId) {
        List<ResponseBookmarkDto> bookMarks = bookMarkService.getBookMarks(userId);
        return ResponseEntity
                .ok(ApiResponse
                        .success(bookMarks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createBookMark(
            @RequestBody @Valid CreateBookMarkDto dto) {
        Long bookMarkId = bookMarkService.createBookMark(dto);
        return ResponseEntity
                .ok(ApiResponse
                        .successLong("북마크 생성 성공", bookMarkId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> toggleBookMark(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBookMarkDto dto) {
        bookMarkService.updateBookMark(id, dto);
        return ResponseEntity
                .ok(ApiResponse
                        .success("북마크 상태 변경 성공", null));
    }
}
