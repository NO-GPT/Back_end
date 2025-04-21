package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.common.Response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class BoardController {

    private final PortfolioRepository portfolioRepository;
    private final BoardService boardService;

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
