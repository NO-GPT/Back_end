package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class BoardController {

    private final BoardService boardService;

    // 게시
    @PostMapping
    public ResponseEntity<?> createPortfolio(@ModelAttribute BoardDto boardDto, List<MultipartFile> files) throws IOException{
        boardDto.setFiles(files);
        return ResponseEntity.ok(boardService.createPortfolio(boardDto));
    }

    // 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<Portfolio>> getAllPortfolio() {
        return ResponseEntity.ok(boardService.getAllPortfolio());
    }

    // 부분 조회
    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable Long id){
        return ResponseEntity.ok(boardService.getPortfolioById(id));
    }

    // 이미지 확인(부분)
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable("id") Long id) {
        return boardService.getFile(id)
                .map(file -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(file.getContentType()))
                        .body(file.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePortfolio(@PathVariable Long id, @ModelAttribute BoardDto boardDto) throws IOException{
        return ResponseEntity.ok(boardService.updatePortfolio(id, boardDto));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long id){
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
