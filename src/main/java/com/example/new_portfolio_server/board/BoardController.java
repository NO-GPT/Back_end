package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Portfolio API", description = "포트폴리오 관련 API")
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
    @Operation(summary = "포트폴리오 생성", description = "포폴을 생성합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(summary = "상세 포트폴리오 조회", description = "상세 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오가 존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResponseBoardDto>> getPortfolioById(
            @Parameter(description = "포폴 아이디", example = "1") @PathVariable Long id) {
        if (!portfolioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }
        ResponseBoardDto portfolio = boardService.getPortfolioById(id);
        return ResponseEntity
                .ok(ApiResponse
                        .success(portfolio));
    }

    @Operation(summary = "모든 포트폴리오 조회", description = "북마크 수 기준 내림차순으로 모든 포트폴리오를 페이징하여 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
    })
    @GetMapping("/top-bookmarked")
    public ResponseEntity<ApiResponse<List<ResponseBoardDto>>> getTopBookmarkedPortfolios(
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {
        List<ResponseBoardDto> portfolios = boardService.getTopBookmarkedPortfolios(page, size);
        return ResponseEntity.ok(ApiResponse.success(portfolios));
    }

    // id값으로 수정
    @Operation(summary = "포트폴리오 조회 수정", description = "수정합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오가 존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(summary = "포트폴리오 삭제", description = "id값으로 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오가 존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
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
