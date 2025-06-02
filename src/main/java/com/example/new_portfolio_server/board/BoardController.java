package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.CursorResponse;
import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class BoardController {
    private final PortfolioRepository portfolioRepository;
    private final BoardService boardService;
    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    // 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchPortfoliosAndUsers(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            List<Map<String, Object>> results = boardService.searchPortfoliosAndUsers(keyword, page, size);
            return ResponseEntity
                    .ok(ApiResponse
                            .success(results));
        } catch (Exception e) {
            logger.error("Search failed for keyword '{}': {}", keyword, e.getMessage());
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse
                            .error("Search failed: " + e.getMessage()));
        }
    }

    // 컬렉션을 다시 초기화하도록 요청하는 엔드포인트
    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindexPortfolios() {
        try {
            boardService.initializeCollections();
            return ResponseEntity
                    .ok(ApiResponse
                            .success("Portfolios reindexed successfully"));
        } catch (Exception e) {
            logger.error("Reindex failed: {}", e.getMessage());
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse
                            .error("Reindex failed: " + e.getMessage()));
        }
    }

    // 게시
    @PostMapping
    public ApiResponse<?> createPortfolio(
            @ModelAttribute @Valid BoardDto boardDto,
            List<MultipartFile> files) throws IOException {
        boardDto.setFiles(files);
        if (boardDto.getFiles() == null || boardDto.getFiles().isEmpty()) {
            return ApiResponse
                    .error("파일이 전달되지 않았습니다.");
        }
        return ApiResponse
                .success(boardService.createPortfolio(boardDto));
    }

    // 커서 기반 페이지네이션
    @GetMapping("/list")
    public List<ResponseBoardDto> getPortfolioSortedByBookmark(
            @RequestParam(required = false) Long cursorBookmarkCount,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int limit
    ){
        return boardService.getAllPortfolioSortedByBookMark(cursorBookmarkCount, cursorId, limit);
    }


    // id값으로 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Portfolio>> getPortfolioById(@PathVariable Long id) {
        if (!portfolioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }
        Portfolio portfolio = boardService.getPortfolioById(id);
        return ResponseEntity
                .ok(ApiResponse
                        .success(portfolio));
    }

    // id값으로 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Portfolio>> updatePortfolio(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateBoardDto boardDto) {
        if (!portfolioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }
        Portfolio updatedPortfolio = boardService.updatePortfolio(id, boardDto);
        return ResponseEntity
                .ok(ApiResponse
                        .success(updatedPortfolio));
    }

    // id값으로 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePortfolio(@PathVariable Long id) {
        if (!portfolioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }
        boardService.delete(id);
        return ResponseEntity
                .ok(ApiResponse
                        .success("포트폴리오가 성공적으로 삭제되었습니다.", null));
    }



}
