package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.CursorResponse;
import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Banner;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.board.file.ImageService;
import com.example.new_portfolio_server.board.repsoitory.BannerRepository;
import com.example.new_portfolio_server.board.repsoitory.FileRepository;
import com.example.new_portfolio_server.board.repsoitory.PortfolioRepository;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
@Tag(name = "Portfolio API", description = "포트폴리오 관련 APIㅇ")
public class BoardController {
    private final PortfolioRepository portfolioRepository;
    private final FileRepository fileRepository;
    private final BannerRepository bannerRepository;

    private final BoardService boardService;
    private final ImageService imageService;

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    // 검색 ㅇㅇ
    @Operation(summary = "포트폴리오 키워드로 검색", description = "키워드가 들어간 포폴들을 조회합니다.",
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
    @Operation(summary = "포트폴리오 생성", description = "포폴을 생성합니다")
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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createPortfolio(
            @RequestPart(value = "boardDto", required = false) BoardDto boardDto,
            @RequestPart(value ="banner", required = false) MultipartFile banner,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        boardDto.setBanner(banner);
        boardDto.setFiles(files);
        return ApiResponse.success(boardService.createPortfolio(boardDto));
    }

    @GetMapping("/file/download")
    public ResponseEntity<?> downloadFile(@RequestParam("fileId") Long fileId) throws IOException {
        return imageService.getObject(fileId);
    }

    // 전체 게시물 조회
    @Operation(summary = "포트폴리오 list", description = " 포폴list 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
    })
    @GetMapping("/list")
    public CursorResponse getPortfolioSortedByLike(
            @RequestParam(required = false) Long likeCount,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "2") int limit
    ){
        return boardService.getAllPortfolioSortedByLike(likeCount, cursorId, limit);
    }

    // username으로 게시글 조회
    @GetMapping("/list/user")
    public ResponseEntity<List<ResponseBoardDto>> getPortfolioByUser(@RequestParam String username){
        return ResponseEntity
                .ok(boardService.getPortfolioByUsername(username));
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
    @GetMapping("/detail/{id}")
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

    // 카테고리별 조회
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<ResponseBoardDto>>> getPortfolioByCategory(
            @RequestParam(required = false) List<String> parts,
            @RequestParam(required = false) List<String> groups,
            @RequestParam(required = false) List<String> skills){
        List<ResponseBoardDto> portfolios = boardService.searchByCategorys(parts, groups, skills);

        if(portfolios == null || portfolios.isEmpty()){
            return ResponseEntity.ok(ApiResponse.fail("해당 카테고리에 해당하는 포트폴리오가 없습니다."));
        }
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
    @PatchMapping("update/{id}")
    public ResponseEntity<ApiResponse<Portfolio>> updatePortfolio(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateBoardDto boardDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {

        if (!portfolioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }

        Portfolio updated = boardService.updatePortfolio(id, boardDto, files, banner);
        return ResponseEntity
                .ok(ApiResponse
                        .success(updated));
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
    @DeleteMapping("/delete/{boardId}")
    public ResponseEntity<ApiResponse<String>> deletePortfolio(@PathVariable Long boardId) {
        if (!portfolioRepository.existsById(boardId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse
                            .error("포트폴리오가 존재하지 않습니다."));
        }
        boardService.delete(boardId);
        return ResponseEntity
                .ok(ApiResponse
                        .success("포트폴리오가 성공적으로 삭제되었습니다.", null));
    }

    // 단일 file만 삭제
    @DeleteMapping("/delete/file")
    public ResponseEntity<ApiResponse<String>> deleteFile(@RequestParam Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

        String fileUrl = file.getFileUrl();
        imageService.deleteFile(fileUrl); // s3 파일 삭제
        fileRepository.deleteById(fileId); // db 파일 삭제

        return ResponseEntity.ok(ApiResponse.success("파일이 성공적으로 삭제되었습니다.", null));
    }

    @DeleteMapping("/delete/banner")
    public ResponseEntity<ApiResponse<String>> deleteBanner(@RequestParam Long bannerId){
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너 파일이 존재하지 않습니다."));

        String bannerUrl = banner.getBannerUrl();
        imageService.deleteFile(bannerUrl);
        bannerRepository.deleteById(bannerId);

        return ResponseEntity.ok(ApiResponse.success("배너 파일이 성공적으로 삭제되었습니다."));
    }
}
