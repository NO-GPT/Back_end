package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchPortfolios(
            @RequestParam("keyword") String keyword) {
        logger.debug("Handling portfolio search with keyword: {}", keyword);
        try {
            List<Map<String, Object>> results = boardService.searchPortfolios(keyword);
            logger.debug("Portfolio search results: {} items", results.size());
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            logger.error("Portfolio search failed for keyword '{}': {}", keyword, e.getMessage(), e);
            String message = e.getMessage().contains("collection not found")
                    ? "Portfolio collection not found. Please reindex data."
                    : "Portfolio search failed: " + e.getMessage();
            return ResponseEntity.status(500).body(ApiResponse.error(message));
        }
    }

    @GetMapping("/search-with-users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchPortfoliosAndUsers(
            @RequestParam("keyword") String keyword) {
        logger.debug("Handling portfolio and users search with keyword: {}", keyword);
        try {
            List<Map<String, Object>> results = boardService.searchPortfoliosAndUsers(keyword);
            logger.debug("Portfolio and users search results: {} items", results.size());
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            logger.error("Portfolio and users search failed for keyword '{}': {}", keyword, e.getMessage(), e);
            String message = e.getMessage().contains("collection not found")
                    ? "Portfolio with users collection not found. Please reindex data."
                    : "Portfolio and users search failed: " + e.getMessage();
            return ResponseEntity.status(500).body(ApiResponse.error(message));
        }
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindexPortfolios() {
        logger.debug("Handling portfolio reindex request");
        try {
            boardService.reindexAllPortfolios();
            logger.debug("Portfolio reindex completed");

            return ResponseEntity
                    .ok(ApiResponse.success("Portfolios reindexed successfully", "요청이 성공적으로 처리되었습니다"));
        } catch (IllegalStateException e) {
            logger.error("Portfolio reindex failed: {}", e.getMessage(), e);

            return ResponseEntity.status(400).body(ApiResponse.error("No portfolios available in database. Please add portfolio data."));
        } catch (Exception e) {
            logger.error("Portfolio reindex failed: {}", e.getMessage(), e);
            String message = e.getMessage().contains("Connection refused")
                    ? "Failed to connect to Typesense server. Please check server status."
                    : "Portfolio reindex failed: " + e.getMessage();

            return ResponseEntity.status(500).body(ApiResponse.error(message));
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

    // 전체 게시물 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Portfolio>>> getAllPortfolio() {
        List<Portfolio> portfolios = boardService.getAllPortfolio();
        return ResponseEntity
                .ok(ApiResponse
                        .success(portfolios));
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
